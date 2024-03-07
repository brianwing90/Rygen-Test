import java.util.List;
import java.util.Map;
import java.util.HashMap;

record Shipment(long shipmentId, List<ShipmentStop> shipmentStops, List<ShipmentLineItem> shipmentLineItems) {}

record ShipmentStop(int sequenceNumber, String address, double milesFromPreviousStop) {}

record ShipmentLineItem(long orderId, String itemDescription, int qty, double weight, int pickupStopSequenceNumber,
                        int dropOffStopSequenceNumber) {}

record CostAllocationResult(long shipmentId, Map<Long, Double> costsByOrderId) {}

interface CostAllocator {
    CostAllocationResult allocateCosts(Shipment shipment, double totalInvoicedAmount);
}

class WeightCostAllocator implements CostAllocator {

    @Override
    public CostAllocationResult allocateCosts(Shipment shipment, double totalInvoicedAmount) {
        if(shipment.shipmentLineItems().isEmpty()) {
            throw new IllegalArgumentException("Shipment has no line items");
        }
        if(shipment.shipmentLineItems().stream().mapToDouble(ShipmentLineItem::weight).min().getAsDouble() <= 0.0) {
            throw new IllegalArgumentException("Invalid weight in shipment");
        }

        double totalWeight = shipment.shipmentLineItems().stream().mapToDouble(ShipmentLineItem::weight).sum();

        Map<Long, Double> costsByOrderId = new HashMap<>();
        for (ShipmentLineItem lineItem : shipment.shipmentLineItems()) {
            double cost = (lineItem.weight() / totalWeight) * totalInvoicedAmount;
            costsByOrderId.merge(lineItem.orderId(), cost, Double::sum);
        }

        return new CostAllocationResult(shipment.shipmentId(), costsByOrderId);
    }
}

class MileageCostAllocator implements CostAllocator {

    @Override
    public CostAllocationResult allocateCosts(Shipment shipment, double totalInvoicedAmount) {
        if(shipment.shipmentStops().isEmpty() || shipment.shipmentLineItems().isEmpty()) {
            throw new IllegalArgumentException("Empty shipment provided");
        }
        if(shipment.shipmentStops().size() == 1) {
            throw new IllegalArgumentException("Cannot calculate cost by mileage with only one stop");
        }
        for(ShipmentStop stop : shipment.shipmentStops()) {
            if(stop.milesFromPreviousStop() == 0.0 && stop.sequenceNumber() > 1) {
                throw new IllegalArgumentException("Intermediate stop with zero distance from previous stop: " + stop.address());
            }
        }

        // Could sort list of stops here and then check if they're sequential or not to avoid errors with invalid stops.

        double totalMiles = 0.0;

        // Could cache results here if there are a very large number of line items to prevent lookups of known mileage later.
        for (ShipmentLineItem lineItem : shipment.shipmentLineItems()) {
            if(lineItem.pickupStopSequenceNumber() >= lineItem.dropOffStopSequenceNumber()) {
                throw new IllegalArgumentException("Invalid pickup/dropoff sequence in line item: " + lineItem.itemDescription());
            }

            totalMiles += shipment.shipmentStops().stream()
                    .filter(stop -> stop.sequenceNumber() > lineItem.pickupStopSequenceNumber() &&
                            stop.sequenceNumber() <= lineItem.dropOffStopSequenceNumber())
                    .mapToDouble(ShipmentStop::milesFromPreviousStop)
                    .sum();
        }

        Map<Long, Double> costsByOrderId = new HashMap<>();
        for (ShipmentLineItem lineItem : shipment.shipmentLineItems()) {
            double shipmentMileage = shipment.shipmentStops().stream()
                    .filter(stop -> stop.sequenceNumber() > lineItem.pickupStopSequenceNumber() &&
                            stop.sequenceNumber() <= lineItem.dropOffStopSequenceNumber())
                    .mapToDouble(ShipmentStop::milesFromPreviousStop)
                    .sum();
            double cost = (shipmentMileage / totalMiles) * totalInvoicedAmount;
            costsByOrderId.merge(lineItem.orderId(), cost, Double::sum);
        }

        return new CostAllocationResult(shipment.shipmentId(), costsByOrderId);
    }
}
