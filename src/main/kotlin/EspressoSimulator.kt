import java.util.*
import java.util.concurrent.ThreadLocalRandom

/**
 * In software company X, engineers work best when consuming one cup of espresso an hour.
 * The office espresso machine has a first-come-first-serve queue that applies to everyone, except for certain
 * "super busy" engineers who are prioritized before non-super-busy ones.  Among competing super-busies the
 * first-come-first-serve principle applies again.
 *
 * Please implement a simulator for this espresso machine. Input parameters are number of engineers, the chance that an
 * engineer becomes super-busy in some unit of time, and for how long they stay super-busy.
 * Feel free to discuss issues that could arise when choosing different parameters.
 */

private const val TIME_TO_MAKE_ESPRESSO = 100L

fun main(args: Array<String>) {

    println("Welcome to the EspressoSimulator.")
    println("How many engineers queueing up for espresso do you want to simulate? Please choose a number between 10 and 500")

    val numberOfEngineers = readLineAsInt()

    println("Generating $numberOfEngineers random engineers...")

    val engineers = (0 until numberOfEngineers).map {
        // For the purpose of this simulation the time frame an engineer can be busy is between the current time and the
        // number of engineers times the time it takes to make one espresso.
        generateRandomEngineer(it, TIME_TO_MAKE_ESPRESSO * numberOfEngineers)
    }

    println("Adding them to the queue.")

    val simulator = EspressoSimulator(TIME_TO_MAKE_ESPRESSO)
    simulator.addToQueue(engineers)

    println("Starting simulation with $numberOfEngineers engineers!")

    simulator.start()

    print("No more engineers in queue! Everyone is happy!\n")

}

/**
 * Data class for engineer with a time frame of him/her being busy in milliseconds.
 */
data class Engineer(
        val id: Int,
        private val busyFrom: Long,
        private val busyTo: Long
) {

    val isBusy: Boolean
        get() = System.currentTimeMillis() in (busyFrom..busyTo)

}

/**
 * Read input and make sure it's an integer between 10 and 1000.
 */
fun readLineAsInt(): Int {
    return try {
        val int = readLine()!!.toInt()
        if (int !in 10..500) throw NumberFormatException() else int
    } catch (e: NumberFormatException) {
        println("This is not a number between 10 and 1000!")
        readLineAsInt()
    }
}

/**
 * Generate a engineer that is busy at a random time.
 */
fun generateRandomEngineer(id: Int, busyToMax: Long): Engineer {
    val now = System.currentTimeMillis()
    val busyFrom = ThreadLocalRandom.current().nextLong(now, now + busyToMax)
    val busyTo = ThreadLocalRandom.current().nextLong(busyFrom, now + busyToMax)
    return Engineer(id, busyFrom, busyTo)
}

class EspressoSimulator(private val timeToMakeEspresso: Long) {

    private val queue: Queue<Engineer> = LinkedList()
    private val priorityQueue: Queue<Engineer> = LinkedList()

    /**
     * Add engineer to either normal or priority queue
     */
    fun addToQueue(engineer: List<Engineer>) {
        engineer.forEach {
            if (it.isBusy) {
                priorityQueue.add(it)
            } else {
                queue.add(it)
            }
        }
    }

    /**
     * Start simulation and run until both queues are empty
     */
    fun start() {
        while (priorityQueue.isNotEmpty() || queue.isNotEmpty()) {
            // Engineers in normal queue could now be busy
            checkPriorities()
            // Make espresso for first in priority queue, otherwise for first in normal queue
            priorityQueue.poll()?.let {
                makeEspresso(it)
            } ?: queue.poll()?.let {
                makeEspresso(it)
            }
        }
    }

    /**
     * Check queue for a busy engineer, move from queue to priority queue
     */
    private fun checkPriorities() {
        queue.find { it.isBusy }?.let {
            queue.remove(it)
            priorityQueue.add(it)
        }
    }

    private fun makeEspresso(engineer: Engineer) {
        Thread.sleep(timeToMakeEspresso)
        print("Made espresso for ${if (engineer.isBusy) "busy " else ""}engineer with ID ${engineer.id}\n")
    }

}
