package object memoryModel {

  def thread(body: => Unit) = {
    val t = new Thread(() => body)
    t.start()
    t
  }
  def log(msg: String) {
    println(s"${Thread.currentThread.getName}: $msg")
  }

}
