package stacks.eliminationStack

class RangePolicy(private val eliminationArrayWidth: Int) {
    private var range = eliminationArrayWidth
    private var successCounter = 0
    private var failCounter = 0

    fun recordEliminationFail() {
        failCounter += 1
        if (failCounter > 10) {
            failCounter = 0
            if (range > 1) {
                range -= 1
            }
        }
    }

    fun recordEliminationSuccess() {
        successCounter += 1
        if (successCounter > 5) {
            successCounter = 0
            if (range < eliminationArrayWidth - 1) {
                range += 1
            }
        }
    }

    fun getRange(): Int {
        return range
    }
}
