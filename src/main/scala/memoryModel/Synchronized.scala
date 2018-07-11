package memoryModel

/*
Synchronized is
Every object in JVM has:
 - intrinsic lock / monitor
 */
object ThreadsSynch extends App {
  def getUniqueId() = this.synchronized { // acquires this monitor
    ThreadsUnprotected.getUniqueId()
  }
  def printUniqueIds(n: Int): Unit = {
    val uids = for (i <- 0 until n) yield getUniqueId()
    println(s"Generated uids: $uids")
  }
  val t = thread { printUniqueIds(5) }
  printUniqueIds(5)
  t.join()
}

object SynchronizedNesting extends App {
  import scala.collection._
  private val transfers = mutable.ArrayBuffer[String]()
  def logTransfer(name: String, n: Int): Unit = transfers.synchronized {
    transfers += s"transfer to account '$name' = $n"
  }
  class Account(val name: String, var money: Int)
  def add(account: Account, n: Int) = account.synchronized {
    account.money += n
    if (n > 10) logTransfer(account.name, n)
  }
  val jane = new Account("Jane", 100)
  val john = new Account("John", 200)
  val t1 = thread { add(jane, 5) }
  val t2 = thread { add(john, 50) }
  val t3 = thread { add(jane, 70) }
  t1.join(); t2.join(); t3.join()
  log(s"--- transfers ---\n$transfers")
}
