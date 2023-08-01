import com.jessecorbett.diskord.bot.*
import com.jessecorbett.diskord.bot.interaction.interactions

class Reward {
    private var dh = mutableMapOf<String, Int>()
    private var elec = mutableMapOf<String, Int>()
    private var countDH = 0
    private var countElec = 0
    private var mapWithDH = mapOf<String, Int>()
    private var mapWithElec = mapOf<String, Int>()
    private var finalResult = ""

    fun setDH(string: String) {
        val list = string.split(' ')
        for (i in list.indices) {
            if (i % 2 == 1) {
                dh[list[i - 1]] = list[i].toInt()
            }
        }
    }
    fun setElec(string: String) {
        val list = string.split(' ')
        for (i in list.indices) {
            if (i % 2 == 1) {
                elec[list[i - 1]] = list[i].toInt()
            }
        }
    }

    fun setCountDH(count: Int) {
        countDH = count
    }
    fun setCountElec(count: Int) {
        countElec = count
    }

    fun nullResources() {
        dh = mutableMapOf()
        elec = mutableMapOf()
        countDH = 0
        countElec = 0
        mapWithDH = mapOf()
        mapWithElec = mapOf()
        finalResult = ""
    }

    fun showResources() : String {
        var str = ""

        str += "Общее количество КБ: $countDH\n"
        str += "Номинанты на получение КБ и их очки:\n${getRes(dh)}"
        str += "\nОбщее количество ящиков с бутылками: $countElec\n"
        str += "Номинанты на получение ящиков с бутылками и их очки:\n${getRes(elec)}"

        return str
    }

    fun getResult() : String {
        mapWithDH = reward(dh, countDH)
        mapWithElec = reward(elec, countElec)
        res()
        return finalResult
    }

    private fun getRes(res: MutableMap<String, Int>) : String {
        var str = ""
        for (i in res) {
            str += "${i.key} ${i.value}\n"
        }
        return str
    }

    private fun res() {
        val result = mutableMapOf<String, String>()

        for (i in mapWithDH) {
            if (i.value != 0) result[i.key] = "${i.value} КБ"
        }

        for (i in mapWithElec) {
            if (i.value != 0) {
                if (result.containsKey(i.key)) {
                    result[i.key] += ", ${i.value} ящиков"
                }
                else {
                    result[i.key] = "${i.value} ящиков"
                }
            }
        }

        val sortedMap = result.toList().sortedByDescending { (_, v) -> v.length }.toMap()

        for (i in sortedMap) {
            finalResult += "${i.key}: ${i.value}\n"
        }
    }

    private fun reward(resource: MutableMap<String, Int>, count: Int) : Map<String, Int> {
        val sum = resource.values.sum()
        val result = mutableMapOf<String, Int>()

        val one = sum / count

        for (i in resource) {
            val rew = i.value / one
            result[i.key] = rew
        }

        val remainder = remainder(resource, one)
        return adding(remainder, result, count - result.values.sum())
    }

    private fun remainder(map: MutableMap<String, Int>, one: Int): Map<String, Int> {
        val result = mutableMapOf<String, Int>()

        for (i in map) {
            result[i.key] = i.value % one
        }
        return result.toList().sortedByDescending { (_, v) -> v }.toMap()
    }

    private fun adding(map: Map<String, Int>, withoutRem: MutableMap<String, Int>, rem: Int): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        var ost = rem

        for (i in map) {
            if (ost != 0) {
                result[i.key] = withoutRem[i.key]!!.plus(1)
                ost--
            } else {
                result[i.key] = withoutRem[i.key]!!
            }
        }
        return result.toList().sortedByDescending { (_, v) -> v }.toMap()
    }
}

suspend fun main() {
    val token = "token"
    val reward = Reward()

    bot(token) {
        interactions {
            slashCommand("kb", "Передать информацию боту об объеме КБ") {
                val countDH by stringParameter("countdh", "Количество КБ")
                callback { interaction, _ ->
                    interaction.respond {
                        reward.setCountDH(countDH.toInt())
                        content = "Общее количество КБ принято."
                    }
                }
            }

            slashCommand("kblist", "Передать информацию боту о номинантах на получение КБ") {
                val listDH by stringParameter("listdh", "Номинанты в формате <ник1 очки1 ник2 очки2 и так далее>")
                callback { interaction, _ ->
                    interaction.respond {
                        reward.setDH(listDH)
                        content = "Номинанты на КБ и их очки приняты."
                    }
                }
            }

            slashCommand("elec", "Передать информацию боту об объеме ящиков с бутылками") {
                val countElec by stringParameter("countelec", "Количество ящиков с бутылками")
                callback { interaction, _ ->
                    interaction.respond {
                        reward.setCountElec(countElec.toInt())
                        content = "Общее количество ящиков с бутылками принято."
                    }
                }
            }

            slashCommand("eleclist", "Передать информацию боту о номинантах на получение ящиков с бутылками") {
                val listElec by stringParameter("listelec", "Номинанты в формате <ник1 очки1 ник2 очки2 и так далее>")
                callback { interaction, _ ->
                    interaction.respond {
                        reward.setElec(listElec)
                        content = "Номинанты на ящики с бутылками и их очки приняты."
                    }
                }
            }

            slashCommand("nullres", "Очистить все параметры") {
                callback { interaction, _ ->
                    interaction.respond {
                        reward.nullResources()
                        content = "Все параметры были очищены."
                    }
                }
            }

            slashCommand("result", "Рассчитать награды и вывести результат") {
                callback { interaction, _ ->
                    interaction.respond {
                        content = reward.getResult()
                    }
                }
            }

            slashCommand("infores", "Показать информацию о введенных данных") {
                callback { interaction, _ ->
                    interaction.respond {
                        content = reward.showResources()
                    }
                }
            }
        }
    }
}