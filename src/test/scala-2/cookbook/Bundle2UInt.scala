// SPDX-License-Identifier: Apache-2.0

package cookbook

import chisel3._
import chisel3.simulator.stimulus.RunUntilFinished

/* ### How do I create a UInt from an instance of a Bundle?
 *
 * Call asUInt on the Bundle instance
 */
class Bundle2UInt extends CookbookTester(1) {
  // Example
  class MyBundle extends Bundle {
    val foo = UInt(4.W)
    val bar = UInt(4.W)
  }
  val bundle = Wire(new MyBundle)
  bundle.foo := 0xc.U
  bundle.bar := 0x3.U
  val uint = bundle.asUInt
  printf(p"$uint") // 195

  // Test
  assert(uint === 0xc3.U)
}

class Bundle2UIntSpec extends CookbookSpec {
  "Bundle2UInt" should "work" in {
    simulate(new Bundle2UInt)(RunUntilFinished(3))
  }
}
