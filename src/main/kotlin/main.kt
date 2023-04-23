import java.util.*

fun main() {
    val account1 = Account(UUID.randomUUID())
    val account2 = Account(UUID.randomUUID())
    account1.deposit(100.0)
    account2.deposit(Stock("KRAS", 100))
    account2.sell(Stock("KRAS", 100), 100.0)
    account1.buy(Stock("KRAS", 100), 100.0)
    stockMarket.matchOrders()
    println(transactions)
    println(stockTransactions)
    println(account1.balance)
    println(account1.stocks)
    println(account2.balance)
    println(account2.stocks)
}