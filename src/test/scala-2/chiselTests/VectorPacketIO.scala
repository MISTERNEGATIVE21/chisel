// SPDX-License-Identifier: Apache-2.0

package chiselTests

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import chisel3.simulator.stimulus.RunUntilFinished
import chisel3.util.Counter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
  * This test used to fail when assignment statements were
  * contained in DeqIO and EnqIO constructors.
  * The symptom is creation of a firrtl file
  * with missing declarations, the problem is exposed by
  * the creation of the val outs in VectorPacketIO
  *
  * NOTE: The problem does not exist now because the initialization
  * code has been removed from DeqIO and EnqIO
  *
  * IMPORTANT:  The canonical way to initialize a decoupled inteface is still being debated.
  */
class Packet extends Bundle {
  val header = UInt(1.W)
}

/**
  * The problem occurs with just the ins or the outs
  * lines also.
  * The problem does not occur if the Vec is taken out
  */
class VectorPacketIO(val n: Int) extends Bundle {
  val ins = Vec(n, chisel3.util.DeqIO(new Packet()))
  val outs = Vec(n, chisel3.util.EnqIO(new Packet()))
}

/**
  * a module uses the vector based IO bundle
  * the value of n does not affect the error
  */
class BrokenVectorPacketModule extends Module {
  val n = 4
  val io = IO(new VectorPacketIO(n))

  // Avoid a "Reference io is not fully initialized" error from firrtl.
  for (i <- 0 until n) {
    io.outs(i) <> io.ins(i)
  }

  /* the following method of initializing the circuit may change in the future */
  io.ins.foreach(_.nodeq())
  io.outs.foreach(_.noenq())
}

class VectorPacketIOUnitTester extends Module {
  val dut = Module(new BrokenVectorPacketModule)
  dut.io <> DontCare

  // This counter just makes the test end quicker
  val c = Counter(1)
  when(c.inc()) {
    stop()
  }
}

class VectorPacketIOUnitTesterSpec extends AnyFlatSpec with ChiselSim {
  "a circuit using an io containing a vector of EnqIO wrapped packets" should
    "compile and run" in {
      simulate {
        new VectorPacketIOUnitTester
      }(RunUntilFinished(3))
    }
}
