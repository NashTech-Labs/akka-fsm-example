package edu.knoldus

import akka.actor.FSM.{CurrentState, SubscribeTransitionCallBack, Transition}
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import edu.knoldus.Inventory.{Open, PoweredOff, Ready}
import edu.knoldus.ProductMessage._
import org.scalatest.funspec.{AnyFunSpec, AnyFunSpecLike}
import org.scalatest.matchers.should

class InventorySpec extends TestKit(ActorSystem("product-system")) with should.Matchers with AnyFunSpecLike with ImplicitSender {

	describe("The Inventory") {

		it("should allow setting and getting of price of product") {
			val inventory = TestActorRef(Props(new Inventory()))
			inventory ! SetCostOfProduct(10)
			inventory ! GetCostOfProduct
			expectMsg(10)
		}

		it("should stay at Transacting when the Deposit is less then the price of the product") {
			val inventory = TestActorRef(Props(new Inventory()))
			inventory ! SetCostOfProduct(10)
			inventory ! SetNumberOfProduct(20)
			inventory ! SubscribeTransitionCallBack(testActor)

			expectMsg(CurrentState(inventory, Open))

			inventory ! Deposit(2)

			inventory ! GetNumberOfProduct

			expectMsg(20)
		}

		it("should transition to ReadyToBuy and then Open when the Deposit is equal to the price of the product") {
			val inventory = TestActorRef(Props(new Inventory()))
			inventory ! SetCostOfProduct(10)
			inventory ! SetNumberOfProduct(20)
			inventory ! SubscribeTransitionCallBack(testActor)

			expectMsg(CurrentState(inventory, Open))

			inventory ! Deposit(10)

			expectMsg(Transition(inventory, Open, Ready))

			inventory ! Product
			expectMsg(Transition(inventory, Ready, Open))

			inventory ! GetNumberOfProduct

			expectMsg(19)
		}


		it("should transition to Open after flushing out all the deposit when the product is canceled") {
			val inventory = TestActorRef(Props(new Inventory()))
			inventory ! SetCostOfProduct(10)
			inventory ! SetNumberOfProduct(20)
			inventory ! SubscribeTransitionCallBack(testActor)

			expectMsg(CurrentState(inventory, Open))

			inventory ! Deposit(5)
			inventory ! Deposit(5)


			expectMsg(Transition(inventory, Open, Ready))

			inventory ! Cancel

			expectMsgPF(){
				case Balance(value)=>value==10
			}

			expectMsg(Transition(inventory, Ready, Open))

			inventory ! GetNumberOfProduct

			expectMsg(20)
		}
		it("should power down if there is no product left.") {
			val inventory = TestActorRef(Props(new Inventory()))
			inventory ! SetCostOfProduct(10)
			inventory ! SetNumberOfProduct(20)

			(1 to 20).foreach{count=>
				inventory ! Deposit(10)
				inventory ! Product
			}

			inventory ! GetNumberOfProduct
			expectMsg(InventoryError("No more Product left"))
		}
	}

}
