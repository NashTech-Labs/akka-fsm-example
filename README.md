## AKKA FSM
<b> A FSM can be described as a set of relations of the form:</b>

```
State(S) x Event(E) -> Actions (A), State(S’)
```
This can be explain as:
```
If we are in state S and the event E occurs, we should perform the actions A and make a transition to the state S’.
```

<b> In this project we demostrate a simple inventory store which uses tha classic Akka FSM. </b> <br>  
<b> An FSM is called a machine because it can only be in one of a finite number of states. <b> <br>

<b> Changing from one state to another is triggered by an event or condition. </b> <br>
<b> It has different number of stages and change their stage as different action performed.This changing of stage is know as Transition </b> <br> 

#### Library Dependencies
```
libraryDependencies ++=Seq(
    "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
      "org.scalatest" %% "scalatest" % "3.2.12" % Test
  )
```
#### Quickstart

1. <b> Clone the repository </b>
2. <b> Compile the project </b>
  ```bash
sbt compile
```
3. <b> Test the project </b>
  ```bash
sbt test
```
4. <b> Compile the project </b>

  ```bash
sbt run
```
