package memoryModel


object ThreadStart extends App {

  val t = Thread.currentThread()
  val name = t.getName
  println(s"I am the thread $name")
}

object ThreadsCreation extends App {
  class MyTHread extends Thread {
    override def run(): Unit = {
      println(s"New Thread running ${Thread.currentThread().getName}")
    }
  }
  val t  = new MyTHread
  t.start()
  t.join() // waits for termination.
  println(s"I am the thread ${Thread.currentThread().getName}")
}

object ThreadsSleep extends App {

  val t = thread {
    Thread.sleep(1000)
    log("New thread running.")
    Thread.sleep(1000)
    log("Still running.")
    Thread.sleep(1000)
    log("Completed.")
  }
  t.join()
  log("New thread joined.")

}

object ThreadsCommunicate extends App {
  var result: String = null
  val t = thread { result = "\nTitle\n" + "=" * 5 }
  t.join()
  log(result)
}

//This is racecondition
//behavior depends on timing at which statements are executed in several threads
object ThreadsUnprotected extends App {
  var uidCount = 0L
  def getUniqueId() = {
    val freshUid = uidCount + 1
    uidCount = freshUid
    freshUid
  }
  def printUniqueIds(n: Int): Unit = {
    val uids = for (i<- 0 until n) yield getUniqueId()
    log(s"Generated uids: $uids")
  }
  val t = thread { printUniqueIds(5) }
  printUniqueIds(5)
  t.join()
}
object ThreadSharedStateReordering extends App {
  for (i <- 0 until 100000) {
    var a = false
    var b = false
    var x = -1
    var y = -1
    val t1 = thread {
      a = true
      y = if(b) 0 else 1
    }
    val t2 = thread {
      b = true
      x = if (a) 0 else 1
    }
    t1.join()
    t2.join()
    assert(!(x == 1 && y == 1), s"x = $x, y = $y")
  }
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

object SynchronizedDeadlock extends App {
  import SynchronizedNesting.Account
  def send(a: Account, b: Account, n: Int) = a.synchronized {
    b.synchronized {
      a.money -= n
      b.money += n
    }
  }
  val a = new Account("Jill", 1000)
  val b = new Account("Jack", 2000)
  val t1 = thread { for (i <- 0 until 100) send(a, b, 1) }
  val t2 = thread { for (i <- 0 until 100) send(b, a, 1) }
  t1.join()
  t2.join()
  log(s"a = ${a.money}, b = ${b.money}")
}
