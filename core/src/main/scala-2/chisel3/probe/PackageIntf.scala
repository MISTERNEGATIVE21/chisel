// SPDX-License-Identifier: Apache-2.0

package chisel3.probe

import chisel3._
import chisel3.experimental.SourceInfo

import scala.language.experimental.macros

private[chisel3] trait Probe$Intf extends SourceInfoDoc {

  /** Access the value of a probe.
    *
    * @param source probe whose value is getting accessed
    */
  def read[T <: Data](source: T): T = macro chisel3.internal.sourceinfo.ProbeTransform.sourceRead[T]

  /** @group SourceInfoTransformMacro */
  def do_read[T <: Data](source: T)(implicit sourceInfo: SourceInfo): T = probe._readImpl(source)
}
