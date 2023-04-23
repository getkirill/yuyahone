package com.kraskaska.economics.yuyahone

import java.util.*

class Economy {
    val transactions = mutableListOf<Transaction>()
    val stockTransactions = mutableListOf<StockTransaction>()
    val stockMarket = StockMarket()
}

data class Transaction(val from: UUID, val to: UUID, val amount: Double)

class Account(val uuid: UUID, val economy: Economy) {
    val balance: Double
        get() = economy.transactions.filter { it.from == uuid || it.to == uuid }
            .sumOf { if (it.from == uuid) -it.amount else it.amount }
    val stocks: List<Stock>
        get() {
            val myStockTransacts = economy.stockTransactions.filter { it.from == uuid || it.to == uuid }
            val myStocks = mutableMapOf<String, Long>()
            myStockTransacts.forEach {
                if (myStocks[it.stock.index] == null) {
                    myStocks[it.stock.index] = 0
                }
                if (it.from == uuid) {
                    myStocks[it.stock.index] = myStocks[it.stock.index]!! - it.stock.amount
                } else {

                    myStocks[it.stock.index] = myStocks[it.stock.index]!! + it.stock.amount
                }
            }
            return myStocks.map { Stock(it.key, it.value) }.filter { it.amount > 0 }
        }

    fun stockBalance(stock: String) = stocks.firstOrNull { it.index == stock } ?: Stock(stock, 0)

    fun send(to: UUID, amount: Double) {
        if (amount <= 0) {
            throw Exception("Amount can't be equal or less than zero you bitch")
        }
        if (balance < amount) {
            throw Exception("Amount can't be more than balance you fuck")
        }
        if (uuid == to) {
            throw Exception("Receiver can't be sender you asshole")
        }
        economy.transactions.add(Transaction(uuid, to, amount))
    }

    fun send(to: UUID, stock: Stock) {
        if (stock.amount <= 0) {
            throw Exception("Amount can't be equal or less than zero you bitch")
        }
        if (stocks.first { it.index == stock.index }.amount < stock.amount) {
            throw Exception("Amount can't be more than balance you fuck")
        }
        if (uuid == to) {
            throw Exception("Receiver can't be sender you asshole")
        }
        economy.stockTransactions.add(StockTransaction(uuid, to, stock))
    }

    fun deposit(amount: Double) {
        economy.transactions.add(Transaction(UUID(0, 0), uuid, amount))
    }

    fun deposit(stock: Stock) {
        economy.stockTransactions.add(StockTransaction(UUID(0, 0), uuid, stock))
    }

    fun withdraw(amount: Double) {
        economy.transactions.add(Transaction(uuid, UUID(0, 0), amount))
    }

    fun withdraw(stock: Stock) {
        economy.stockTransactions.add(StockTransaction(uuid, UUID(0, 0), stock))
    }

    fun buy(stock: Stock, price: Double) {
        economy.stockMarket.activeOrders.add(StockOrder(StockOrder.Operation.BUY, stock, price, this))
    }

    fun sell(stock: Stock, price: Double) {
        economy.stockMarket.activeOrders.add(StockOrder(StockOrder.Operation.SELL, stock, price, this))
    }
}