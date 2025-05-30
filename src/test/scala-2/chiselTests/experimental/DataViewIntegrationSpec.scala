// See LICENSE for license details.

package chiselTests.experimental

import chisel3._
import chisel3.experimental.dataview._
import chisel3.testing.scalatest.FileCheck
import chisel3.util.{log2Ceil, Decoupled, DecoupledIO, Queue, QueueIO}
import circt.stage.ChiselStage
import firrtl.transforms.DontTouchAnnotation
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

// Let's put it all together!
object DataViewIntegrationSpec {

  class QueueIntf[T <: Data](gen: T, entries: Int) extends Bundle {
    val ports = new QueueIO(gen, entries)
    // Let's grab a reference to something internal too
    // Output because can't have directioned and undirectioned stuff
    val enq_ptr = Output(UInt(log2Ceil(entries).W))
  }

  // It's not clear if a view of a Module ever _can_ be total since internal nodes are part of the Module
  implicit def queueView[T <: Data] = PartialDataView[Queue[T], QueueIntf[T]](
    q => new QueueIntf(q.gen, q.entries),
    _.io -> _.ports,
    // Some token internal signal
    _.enq_ptr.value -> _.enq_ptr
  )

  object MyQueue {
    def apply[T <: Data](enq: DecoupledIO[T], n: Int): QueueIntf[T] = {
      val queue = Module(new Queue[T](enq.bits.cloneType, n))
      val view = queue.viewAs[QueueIntf[T]]
      view.ports.enq <> enq
      view
    }
  }

  class MyModule extends Module {
    val enq = IO(Flipped(Decoupled(UInt(8.W))))
    val deq = IO(Decoupled(UInt(8.W)))

    val queue = MyQueue(enq, 4)
    deq <> queue.ports.deq
    dontTouch(queue.enq_ptr)
  }
}

class DataViewIntegrationSpec extends AnyFlatSpec with Matchers with FileCheck {
  import DataViewIntegrationSpec.MyModule

  "Users" should "be able to view and annotate Modules" in {
    ChiselStage
      .emitCHIRRTL(new MyModule)
      .fileCheck()(
        """CHECK: "target":"~|Queue4_UInt8>enq_ptr_value""""
      )
  }
}
