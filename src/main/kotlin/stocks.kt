import java.util.*


data class StockTransaction(val from: UUID, val to: UUID, val stock: Stock)
data class Stock(val index: String, val amount: Long) {
    operator fun compareTo(stock: Stock): Int {
        return amount.compareTo(stock.amount)
    }
}

data class StockOrder(val op: Operation, val stock: Stock, val price: Double, val account: Account) {
    val pricePerStock get() = price / stock.amount

    enum class Operation {
        BUY, SELL
    }

    fun subtract(order: StockOrder): StockOrder {
        val amount = stock.amount - order.stock.amount
        val price = price - pricePerStock * order.stock.amount
        return StockOrder(op, stock.copy(amount = amount), price, account)
    }

    operator fun minus(other: StockOrder) = subtract(other)
}


class StockMarket {
    val activeOrders = mutableListOf<StockOrder>()

    fun matchOrders() {
        fixOrders()
        activeOrders.filter { it.op == StockOrder.Operation.BUY }.forEach { buyOrder ->
            val bestSellOrder =
                activeOrders.filter { it.op == StockOrder.Operation.SELL && it.stock.index == buyOrder.stock.index && buyOrder.pricePerStock >= it.pricePerStock && it.stock.amount >= buyOrder.stock.amount }
                    .minByOrNull { it.pricePerStock } ?: return@forEach
            if (buyOrder.stock.amount > bestSellOrder.stock.amount) {
                // Calculate the maximum to fulfill and push the rest to active orders.
                val newBuyOrder = buyOrder - bestSellOrder
                bestSellOrder.account.send(buyOrder.account.uuid, buyOrder.stock)
                buyOrder.account.send(bestSellOrder.account.uuid, buyOrder.pricePerStock * bestSellOrder.stock.amount)
                activeOrders.remove(buyOrder)
                activeOrders.remove(bestSellOrder)
                activeOrders.add(newBuyOrder)
            } else if (bestSellOrder.stock.amount >= buyOrder.stock.amount) {
                // Calculate how much fullfilled and subtract from the sell order.
                val newSellOrder = bestSellOrder - buyOrder
                bestSellOrder.account.send(buyOrder.account.uuid, buyOrder.stock)
                buyOrder.account.send(bestSellOrder.account.uuid, buyOrder.price)
                activeOrders.remove(buyOrder)
                activeOrders.remove(bestSellOrder)
                activeOrders.add(newSellOrder)
            }
        }
        fixOrders()
    }

    fun fixOrders() {
        // Remove empty orders
        activeOrders.filter {it.stock.amount <= 0}.forEach {activeOrders.remove(it)}
        // Remove sell orders that sell more than balance
        activeOrders.filter {it.op == StockOrder.Operation.SELL && it.account.stockBalance(it.stock.index) > it.stock}.forEach {activeOrders.remove(it)}
    }

    fun lowestPrice(stock: String) =
        activeOrders.filter { it.op == StockOrder.Operation.SELL && it.stock.index == stock }
            .minOfOrNull { it.pricePerStock }

    fun averagePrice(stock: String) =
        activeOrders.filter { it.op == StockOrder.Operation.SELL && it.stock.index == stock }.map { it.pricePerStock }
            .average()
}