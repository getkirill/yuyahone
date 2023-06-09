import com.kraskaska.economics.yuyahone.Account
import com.kraskaska.economics.yuyahone.Economy
import com.kraskaska.economics.yuyahone.Stock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.*
import kotlin.test.assertEquals

internal class Tests {
    var economy = Economy()
    @BeforeEach
    fun newEconomy() {
        economy = Economy()
    }
    @Test
    fun testSimpleTransaction() {
        val account1 = Account(UUID.randomUUID(), economy)
        val account2 = Account(UUID.randomUUID(), economy)

        account1.deposit(5.0)
        account1.send(account2.uuid, 4.0)
        account2.withdraw(3.0)
        assert(account1.balance == 1.0)
        assert(account2.balance == 1.0)
    }

    @Test
    fun testStockTransaction() {
        val account1 = Account(UUID.randomUUID(), economy)
        val account2 = Account(UUID.randomUUID(), economy)

        account1.deposit(Stock("A", 5))
        account1.send(account2.uuid, Stock("A", 4))
        account2.withdraw(Stock("A", 3))
        assert(account1.stockBalance("A").amount == 1L)
        assert(account2.stockBalance("A").amount == 1L)
    }

    @Test
    fun testStockMarket() {
        val account1 = Account(UUID.randomUUID(), economy)
        val account2 = Account(UUID.randomUUID(), economy)

        account1.deposit(360.0)
        account2.deposit(Stock("SANE", 100))
        account2.sell(Stock("SANE", 100), 345.0)
        assertDoesNotThrow {
            economy.stockMarket.activeOrders.first()
        }
        account1.buy(Stock("SANE", 100), 360.0)
        assert(economy.stockMarket.activeOrders.size == 2)
        economy.stockMarket.matchOrders()
        assert(economy.stockMarket.activeOrders.size == 0)
        assert(account1.stockBalance("SANE").amount == 100L)
        assert(account2.stockBalance("SANE").amount == 0L)
        assert(account2.balance == 360.0)
        assert(account1.balance == 0.0)
    }

    @Test
    fun testStockMarketMultipleOrders() {
        val account1 = Account(UUID.randomUUID(), economy)
        val account2 = Account(UUID.randomUUID(), economy)
        val account3 = Account(UUID.randomUUID(), economy)

        // Account 1 is buyer, account 2/3 are sellers

        account1.deposit(100.0)
        account2.deposit(Stock("A", 10))
        account3.deposit(Stock("A", 10))

        account1.buy(Stock("A", 10), 100.0)
        account2.sell(Stock("A", 10), 100.0)
        account3.sell(Stock("A", 10), 50.0)

        println("${economy.stockMarket.averagePrice("A")}, ${economy.stockMarket.lowestPrice("A")}")

        economy.stockMarket.matchOrders()

        assertEquals(account1.stockBalance("A").amount, 10)
        assertEquals(account2.stockBalance("A").amount, 10)
        assertEquals(account3.stockBalance("A").amount, 0)
        assertEquals(account1.balance, 0.0)
        assertEquals(account2.balance, 0.0)
        assertEquals(account3.balance, 100.0)
    }
}