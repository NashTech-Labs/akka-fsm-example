package edu.knoldus

object ProductMessage {

	trait UserInteraction
	trait VendorInteraction

	case class Deposit(value: Int) extends UserInteraction
	case class Balance(value: Int) extends UserInteraction
	case object Cancel extends UserInteraction
	case object Product extends UserInteraction
	case object GetCostOfProduct extends UserInteraction

	case object ShutDownInventory extends VendorInteraction
	case object StartUpInventory extends VendorInteraction
	case class SetNumberOfProduct(quantity: Int) extends VendorInteraction
	case class SetCostOfProduct(price: Int) extends VendorInteraction
	case object GetNumberOfProduct extends VendorInteraction

	case class InventoryError(errorMsg:String)

}

