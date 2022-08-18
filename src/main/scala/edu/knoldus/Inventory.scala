package edu.knoldus

import akka.actor.FSM
import edu.knoldus.Inventory.{InventoryData, InventoryState, Open, PoweredOff, Ready}
import edu.knoldus.ProductMessage.{Balance, Cancel, Deposit, GetCostOfProduct, GetNumberOfProduct, InventoryError, Product, SetCostOfProduct, SetNumberOfProduct, ShutDownInventory, StartUpInventory}
import org.slf4j.LoggerFactory

object Inventory {

	sealed trait InventoryState
	case object Open extends InventoryState
	case object Ready extends InventoryState
	case object PoweredOff extends InventoryState
	case class InventoryData(currentTxTotal: Int, costOfProduct: Int, productLeft: Int)

}
class Inventory extends FSM[InventoryState, InventoryData] {

	private val logger = LoggerFactory.getLogger(classOf[Inventory])

	startWith(Open, InventoryData(currentTxTotal = 0, costOfProduct=  20, productLeft = 45))

	when(Open) {
		case Event(_,InventoryData(_,_,productLeft)) if productLeft<=0 =>
			logger.warn("No more Product Left")
			sender ! InventoryError("No more Product left")
			goto(PoweredOff)
		case Event(Deposit(value), InventoryData(currentTxTotal, _, _)) if (value + currentTxTotal) >= stateData.costOfProduct =>
			goto(Ready) using stateData.copy(currentTxTotal = currentTxTotal + value)

		case Event(Deposit(value), InventoryData(currentTxTotal, _, _)) if (value + currentTxTotal) < stateData.costOfProduct =>
			val cvalue = currentTxTotal + value
			logger.debug(s"staying at open with value $cvalue")
			stay using stateData.copy(currentTxTotal = cvalue)
		case Event(SetNumberOfProduct(quantity), _) => stay using stateData.copy(productLeft = quantity)
		case Event(GetNumberOfProduct, _) => sender ! stateData.productLeft; stay()
		case Event(SetCostOfProduct(price), _) => stay using stateData.copy(costOfProduct = price)
		case Event(GetCostOfProduct, _) => sender ! stateData.costOfProduct; stay()
	}

	when(Ready) {
		case Event(Product, InventoryData(currentTxTotal, costOfProduct, productLeft)) =>
			val balance = currentTxTotal - costOfProduct
			logger.debug(s"Balance is $balance")
			if (balance> 0) {
				sender ! Balance(value = balance)
				goto(Open) using stateData.copy(currentTxTotal = 0, productLeft = productLeft - 1)
			}
			else goto(Open) using stateData.copy(currentTxTotal = 0, productLeft = productLeft - 1)
	}

	when(PoweredOff) {
		case Event(StartUpInventory, _) => goto(Open)
		case _ =>
			logger.warn("Inventory is stop.  Please start inventory first with StartUpInventory")
			sender ! InventoryError("Inventory is stop.  Please start inventory first with StartUpInventory")
			stay()
	}

	whenUnhandled {
		case Event(ShutDownInventory, InventoryData(currentTxTotal, _, _)) =>
			sender ! Balance(value = currentTxTotal)
			goto(PoweredOff) using stateData.copy(currentTxTotal = 0)
		case Event(Cancel, InventoryData(currentTxTotal, _, _)) =>
			logger.debug(s"Balance is $currentTxTotal")
			sender ! Balance(value = currentTxTotal)
			goto(Open) using stateData.copy(currentTxTotal = 0)
	}
	onTransition {
		case Open -> Ready=> logger.debug("From Transacting to Ready")
		case Ready -> Open => logger.debug("From Ready to Open")
	}
}

