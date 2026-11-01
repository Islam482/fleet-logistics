# Defense notes (for me, not for grading)

**Why can't the client call `visitor.visit(node)`?** The client holds a `FleetElement`. Java picks the overload at compile time from the static type, so only `visit(FleetElement)` would be looked for, and it does not exist. `node.accept(v)` is a virtual call (first dispatch, on the node's runtime class). Inside `Truck.accept`, `this` is statically `Truck`, so `v.visit(this)` binds to `visit(Truck)` (second dispatch). Show `Truck.java` and `DoubleDispatchTest`.

**New-element commit (`CargoBike`).** 9 files: `CargoBike`, `FleetVisitor`, 5 visitors, 2 tests. Reject Visitor when element types change often or there are many visitors.

**Unsubscribe while threads publish.** `EventHub.registry` is a read-only `EnumMap` of `CopyOnWriteArrayList`. `publish` iterates a snapshot, `unsubscribe` swaps in a new array. No exception, no skipped neighbour. The removed observer may get the one event already in flight. Show `EventHubTest.unsubscribingWhileSeveralThreadsPublish...` and `ConcurrencyTest.fourThreads...`.

**Likely live-coding tasks.** (1) New visitor: copy the `FuelCostVisitor` pattern, extend `FoldingVisitor<R>`, implement 4 leaf methods. (2) New listener: implement `Observer<E>`, add one `subscribe` line in `CompositionRoot`. (3) New event type: add to `EventType`, a record, publish it from a source. (4) Change a limit: constants in `InspectionVisitor`, `CompositionRoot.LOW_FUEL_LITERS`.
