# Игра "жизнь"

Исследуем код `gol_data.c` на ошибки параллельного программирования. Для начала пару слов о том, какая функциональность предполагалась. 

Запускается 3 потока, совершающие для общего прогресса следующие действия:
1. Поток, обходящий все поле и кладущий в очереди для обработки живые и мертвые клетки  
2. Читает очередь из живых необработанных клеток и обновляет карту для следующей итерации
3. Читает очередь из мертвых необработанных клеток и обновляет карту для следующей итерации.
 
Используем `Helgrind` для исследования. Для запуска вводилось "valgrind --tool=helgrind -s ./gol_task 100 100". Весь выход в файле `out.txt`. Инструмент нашел некоторые ошибки, самые занятные из них:

<details>
  <summary>Out</summary>

  ```
 Possible data race during write of size 4 at 0x10D078 by thread #4
 Locks held: 1, at address 0x116D20
    at 0x109B5D: readDeadQueue (in /root/programming/pp-hw/CGOL-Pthread-Parallelization/gol_task)
    by 0x485396A: ??? (in /usr/libexec/valgrind/vgpreload_helgrind-amd64-linux.so)
    by 0x48FEAC2: start_thread (pthread_create.c:442)
    by 0x498FA03: clone (clone.S:100)
 
 This conflicts with a previous read of size 4 by thread #3
 Locks held: none
    at 0x109A76: readLiveQueue (in /root/programming/pp-hw/CGOL-Pthread-Parallelization/gol_task)
    by 0x485396A: ??? (in /usr/libexec/valgrind/vgpreload_helgrind-amd64-linux.so)
    by 0x48FEAC2: start_thread (pthread_create.c:442)
    by 0x498FA03: clone (clone.S:100)
  Address 0x10d078 is 0 bytes inside data symbol "deadWrites"
  ```
</details>
<details>
<summary>Код функций</summary>

```C++
void* readLiveQueue(void* arg) {
    for (int j = 0; j < numIterations; j++) {
        while ((aliveWrites + deadWrites) < totalSize) {
            //queue is empty
            pthread_mutex_lock(&liveMutex);
            while (isempty(liveQueue) == 1) {
                pthread_cond_wait(&liveCond, &liveMutex);
            }
            Node* node = dequeue(liveQueue);
            updateNextBoard(nextBoard, node->location, 1);
            free(node);
            aliveWrites++;
            pthread_mutex_unlock(&liveMutex);

        }
        while (iterSynch[j] != 1) {
            //busy wait
        }
    }
    return((void*)0);
}

void* readDeadQueue(void* arg) {
    for (int j = 0; j < numIterations; j++) {
        while ((aliveWrites + deadWrites) < totalSize) {
            //queue is empty
            pthread_mutex_lock(&deadMutex);
            while (isempty(deadQueue) == 1) {
                pthread_cond_wait(&deadCond, &deadMutex);
            }
            Node* node = dequeue(deadQueue);
            updateNextBoard(nextBoard, node->location, 0);
            free(node);
            deadWrites++;
            pthread_mutex_unlock(&deadMutex);
        }
        while (iterSynch[j] != 1) {
            //busy wait
        }
    }
    return((void*)0);
}
```
</details>

Что занятного в этих функциях? Они обе ссылаются на переменные `aliveWrites` и `deadWrites`, однако их обновление происходит небезопасно (хотя можно было бы использовать CAS), из-за чего возможен неблагоприятный сценарий, когда при записи нового значения переменной другой поток не может прочитать текущее значение переменной — или же гонка данных. Эта проблема в коде встречается почти повсеместно. Возможное решение проблемы: добавление CAS. Были изменены функции `aliveWrites`, `deadWrites`, `main` (код, что идет после создания процессов) и `swapBoards`. Логи — в файле `out2.txt`. Как итог, количество ошибок снизилось с 6938623 до 990876 (не похоже на погрешность). 

-----
Помимо очевидных ошибок инструмент выдает и ложноположительные заключения: 

<details>
<summary>Out</summary>

```
    Possible data race during read of size 1 at 0x116CE8 by thread #1
    Locks held: none
    at 0x485077C: ??? (in /usr/libexec/valgrind/vgpreload_helgrind-amd64-linux.so)
    by 0x4850A2A: ??? (in /usr/libexec/valgrind/vgpreload_helgrind-amd64-linux.so)
    by 0x10987D: main (in /root/programming/pp-hw/CGOL-Pthread-Parallelization/gol_task)
    
    This conflicts with a previous write of size 4 by thread #3
    Locks held: none
    at 0x4903A90: __pthread_mutex_unlock_usercnt (pthread_mutex_unlock.c:62)
    by 0x4903A90: pthread_mutex_unlock@@GLIBC_2.2.5 (pthread_mutex_unlock.c:368)
    by 0x4851248: ??? (in /usr/libexec/valgrind/vgpreload_helgrind-amd64-linux.so)
    by 0x109BB9: readLiveQueue (in /root/programming/pp-hw/CGOL-Pthread-Parallelization/gol_task)
    by 0x485396A: ??? (in /usr/libexec/valgrind/vgpreload_helgrind-amd64-linux.so)
    by 0x48FEAC2: start_thread (pthread_create.c:442)
    by 0x498FA03: clone (clone.S:100)
    Address 0x116ce8 is 8 bytes inside data symbol "liveMutex"
```
Судя по тому, что он ругается на свой so и освобождение мьютекса, проблема не в реализации алгоритма.
</details>

-----
Еще ошибка связанная с недостающей синхронизацией:
<details>
<summary>Out</summary>
 
  Lock at 0x116CE0 was first observed
    at 0x4854BFE: pthread_mutex_init (in /usr/libexec/valgrind/vgpreload_helgrind-amd64-linux.so)
    by 0x10947C: main (in /root/programming/pp-hw/CGOL-Pthread-Parallelization/gol_task)
  Address 0x116ce0 is 0 bytes inside data symbol "liveMutex"
 
 Possible data race during read of size 4 at 0x4A9FE50 by thread #2
 Locks held: none
    at 0x10A27C: computeNeighbours (in /root/programming/pp-hw/CGOL-Pthread-Parallelization/gol_task)
    by 0x1099C7: queueThread (in /root/programming/pp-hw/CGOL-Pthread-Parallelization/gol_task)
    by 0x485396A: ??? (in /usr/libexec/valgrind/vgpreload_helgrind-amd64-linux.so)
    by 0x48FEAC2: start_thread (pthread_create.c:442)
    by 0x498FA03: clone (clone.S:100)
 
 This conflicts with a previous write of size 4 by thread #3
 Locks held: 1, at address 0x116CE0
    at 0x10A36C: updateNextBoard (in /root/programming/pp-hw/CGOL-Pthread-Parallelization/gol_task)
    by 0x109B5C: readLiveQueue (in /root/programming/pp-hw/CGOL-Pthread-Parallelization/gol_task)
    by 0x485396A: ??? (in /usr/libexec/valgrind/vgpreload_helgrind-amd64-linux.so)
    by 0x48FEAC2: start_thread (pthread_create.c:442)
    by 0x498FA03: clone (clone.S:100)
  Address 0x4a9fe50 is 400 bytes inside a block of size 40,000 alloc'd
    at 0x484A919: malloc (in /usr/libexec/valgrind/vgpreload_helgrind-amd64-linux.so)
    by 0x10A012: createSecondBoard (in /root/programming/pp-hw/CGOL-Pthread-Parallelization/gol_task)
    by 0x1095EC: main (in /root/programming/pp-hw/CGOL-Pthread-Parallelization/gol_task)
  Block was alloc'd by thread #1
 
 
 302 errors in context 10 of 12:
</details>
</br>

Ошибка связана с методами `updateNextBoard` и `computeNeighbours`, которые одновременно изменяют карту без достаточной синхронизации. 

-----
<b>Наличие гонок данных было продемонстрировано ранее.</b>