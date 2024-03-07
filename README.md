# Rygen Coding Assessment

The sales team just closed a deal for a huge new customer! That's good! As part of this deal, we're required to stand up a new API endpoint. And the customer is expecting this API to be ready by the end of business today! That's bad!

Okay, that’s a bit ridiculous. As a new company, we have a lot of constraints on our time and resources, which requires us to take a very pragmatic approach to development.
In this exercise, we want you to build a solution based on the requirements below without spending too much time on it.
Although this is an exercise, approach it as you would a real-life project at Rygen.
You’ll then have a one hour meeting with a small group of Rygen developers to present your solution. There are no requirements for the presentation, and you don’t need to send us any code or commit it anywhere. Just be ready to share your screen when we meet and to walk through your work.

This is not an attempt to trap you with some impossible leetcode task. If you feel the requirements are unclear, you can just make some assumptions and move on with the task. Just make note of what assumptions you make and we can discuss them when we go through the code.

This exercise and presentation will help us assess how you think about problems, write code, adhere to requirements, make trade-offs, and communicate with a team.

## Product Requirements Document

### Background

In our system, users can consolidate multiple orders to be executed by a single shipment. On the ShipmentLineItem object, each line item references which order it came from. A shipment can have multiple line items from multiple orders (including multiple line items from a single order)

A single shipment will receive a single invoice which contains a totalCost.

(Note: We're using doubles for simplicity, but the costs are meant to be usd money. USD only has two decimal places).

### Requirements

Users need to be able to report on how much each order costs.

Costs can be allocated to orders in one of two ways: by weight or by miles.

The examples below are written in java, but you may use whatever language you're most comforitable with.

We expect passing tests, but no additional documentation is required.

Consider likely error cases, and either handle them or be prepared to discuss.

```java
@Getter
@AllArgsConstructor
class Shipment {
    private final long shipmentId;
    private final List<ShipmentStop> shipmentStops;
    private final List<ShipmentLineItem> shipmentLineItems;
}

@Getter
@AllArgsConstructor
class ShipmentStop {
    private final int sequenceNumber;
    private final String address;
    private final double milesFromPreviousStop;
}

@Getter
@AllArgsConstructor
class ShipmentLineItem {
    private final long orderId;
    private final String itemDescription;
    private final int qty;
    private final double weight;
    private final int pickupStopSequenceNumber;
    private final int dropOffStopSequenceNumber;
}
```

Implement the `CostAllocator` interface for the two cost allocation methods.

```java
record CostAllocationResult(
        long shipmentId,
        Map<Long, Double> costsByOrderId) {}

interface CostAllocator {
    CostAllocationResult allocateCosts(Shipment shipment, double totalInvoicedAmount);
}
```



Example:

```json
{
  "shipmentId": 1,
  "shipmentStops": [
    {
      "sequenceNumber": 1,
      "address": "The White House",
      "milesFromPreviousStop": 0
    },
    {
      "sequenceNumber": 2,
      "address": "Bob's House",
      "milesFromPreviousStop": 425
    },
    {
      "sequenceNumber": 3,
      "address": "World of Coca-Cola",
      "milesFromPreviousStop": 123
    }
  ],
  "shipmentLineItems": [
    {
      "orderId": 1,
      "itemDescription": "tigers",
      "qty": 10,
      "weight": 100,
      "pickupStopSequence": 1,
      "dropOffStopSequence": 3
    },
    {
      "orderId": 1,
      "itemDescription": "lions",
      "qty": 10,
      "weight": 125,
      "pickupStopSequence": 1,
      "dropOffStopSequence": 2
    },
    {
      "orderId": 2,
      "itemDescription": "bears",
      "qty": 5,
      "weight": 150,
      "pickupStopSequence": 2,
      "dropOffStopSequence": 3
    }
  ]
}
```




```
                   ┌──────425mi──────┐  ┌───────123mi──────┐      
                   │                 ▼  │                  ▼      
                SEQ1                 SEQ2                  SEQ3   
SHIPMENT 1      WHITEHOUSE──────────►BOBSHOUSE────────────►COCACOLA

ORDER 1 TIGERS  PICKUP────────────────────────────────────►DROPOFF
ORDER 1 LIONS   PICKUP──────────────►DROPOFF                        
ORDER 2 BEARS                        PICKUP───────────────►DROPOFF

```

totalInvoicedAmount = 1234.34

byWeight: {1: 740.60, 2: 493.74}

byMiles: {1: 1095.82, 2: 138.52}
