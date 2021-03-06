package lsq.port

import chisel3._
import chisel3.util._
import lsq.config.LsqConfigs

class StoreDataPort(config: LsqConfigs) extends Module {
  override def desiredName: String = "STORE_DATA_PORT"

  val io = IO(new Bundle {
    // interface to previous
    val dataFromPrev = Flipped(Decoupled(UInt(config.dataWidth.W)))
    // interface to GA
    val portEnable = Input(Bool())

    // interface to LQ
    val storeDataEnable = Output(Bool())
    val dataToStoreQueue = Output(UInt(config.dataWidth.W))
  })

  val cnt = RegInit(0.U(log2Ceil(config.fifoDepth + 1).W))

  // updating counter
  when(io.portEnable && !io.storeDataEnable && cnt =/= config.fifoDepth.U) {
    cnt := cnt + 1.U
  }.elsewhen(io.storeDataEnable && !io.portEnable && cnt =/= 0.U) {
    cnt := cnt - 1.U
  }

  io.dataToStoreQueue := io.dataFromPrev.bits
  io.storeDataEnable := cnt > 0.U && io.dataFromPrev.valid
  io.dataFromPrev.ready := cnt > 0.U
}
