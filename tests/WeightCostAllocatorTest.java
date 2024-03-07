import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeightCostAllocatorTest {

    @Test
    void testGoodShipment() {
        Shipment shipment = new Shipment(
                1,
                List.of(
                        new ShipmentStop(1, "The White House", 0),
                        new ShipmentStop(2, "Bob's House", 425),
                        new ShipmentStop(3, "World of Coca-Cola", 123)
                ),
                List.of(
                        new ShipmentLineItem(1, "tigers", 10, 100, 1, 3),
                        new ShipmentLineItem(1, "lions", 10, 125, 1, 2),
                        new ShipmentLineItem(2, "bears", 5, 150, 2, 3)
                )
        );

        CostAllocator byWeightAllocator = new WeightCostAllocator();
        CostAllocationResult byWeightResult = byWeightAllocator.allocateCosts(shipment, 1234.34);

        assertEquals(740.60, byWeightResult.costsByOrderId().get(1L), 0.01);
        assertEquals(493.74, byWeightResult.costsByOrderId().get(2L), 0.01);
    }

    @Test
    void testEmptyShipment() {
        Shipment shipment = new Shipment(
                1,
                List.of(),
                List.of()
        );

        CostAllocator byWeightAllocator = new WeightCostAllocator();
        assertThrows(IllegalArgumentException.class, () -> byWeightAllocator.allocateCosts(shipment, 1234.56));
    }

    @Test
    void testOneStop() {
        Shipment shipment = new Shipment(
                1,
                List.of(
                        new ShipmentStop(1, "The White House", 0)
                ),
                List.of(
                        new ShipmentLineItem(1, "tigers", 10, 100, 1, 3),
                        new ShipmentLineItem(1, "lions", 10, 125, 1, 2),
                        new ShipmentLineItem(2, "bears", 5, 150, 2, 3)
                )
        );

        CostAllocator byWeightAllocator = new WeightCostAllocator();
        CostAllocationResult byWeightResult = byWeightAllocator.allocateCosts(shipment, 1234.34);

        assertEquals(740.60, byWeightResult.costsByOrderId().get(1L), 0.01);
        assertEquals(493.74, byWeightResult.costsByOrderId().get(2L), 0.01);
    }

    @Test
    void testOneLineItem() {
        Shipment shipment = new Shipment(
                1,
                List.of(
                        new ShipmentStop(1, "The White House", 0),
                        new ShipmentStop(2, "Bob's House", 425),
                        new ShipmentStop(3, "World of Coca-Cola", 123)
                ),
                List.of(
                        new ShipmentLineItem(1, "tigers", 10, 100, 1, 3)
                )
        );

        CostAllocator byWeightAllocator = new WeightCostAllocator();
        CostAllocationResult byWeightResult = byWeightAllocator.allocateCosts(shipment, 1234.34);

        assertEquals(1234.34, byWeightResult.costsByOrderId().get(1L), 0.01);
    }

    @Test
    void testZeroTotalInvoice() {
        Shipment shipment = new Shipment(
                1,
                List.of(
                        new ShipmentStop(1, "The White House", 0),
                        new ShipmentStop(2, "Bob's House", 425),
                        new ShipmentStop(3, "World of Coca-Cola", 123)
                ),
                List.of(
                        new ShipmentLineItem(1, "tigers", 10, 100, 1, 3),
                        new ShipmentLineItem(1, "lions", 10, 125, 1, 2),
                        new ShipmentLineItem(2, "bears", 5, 150, 2, 3)
                )
        );

        CostAllocator byWeightAllocator = new WeightCostAllocator();
        CostAllocationResult byWeightResult = byWeightAllocator.allocateCosts(shipment, 0.0);

        assertEquals(0.0, byWeightResult.costsByOrderId().get(1L), 0.01);
        assertEquals(0.0, byWeightResult.costsByOrderId().get(2L), 0.01);
    }

    @Test
    void testZeroWeight() {
        Shipment shipment = new Shipment(
                1,
                List.of(
                        new ShipmentStop(1, "The White House", 0),
                        new ShipmentStop(2, "Bob's House", 425),
                        new ShipmentStop(3, "World of Coca-Cola", 123)
                ),
                List.of(
                        new ShipmentLineItem(1, "tigers", 10, 0, 1, 3),
                        new ShipmentLineItem(1, "lions", 10, 0, 1, 2),
                        new ShipmentLineItem(2, "bears", 5, 0, 2, 3)
                )
        );

        CostAllocator byWeightAllocator = new WeightCostAllocator();
        assertThrows(IllegalArgumentException.class, () -> byWeightAllocator.allocateCosts(shipment, 1234.56));
    }

    @Test
    void testNegativeWeight() {
        Shipment shipment = new Shipment(
                1,
                List.of(
                        new ShipmentStop(1, "The White House", 0),
                        new ShipmentStop(2, "Bob's House", 425),
                        new ShipmentStop(3, "World of Coca-Cola", 123)
                ),
                List.of(
                        new ShipmentLineItem(1, "tigers", 10, -100, 1, 3),
                        new ShipmentLineItem(1, "lions", 10, -125, 1, 2),
                        new ShipmentLineItem(2, "bears", 5, -150, 2, 3)
                )
        );

        CostAllocator byWeightAllocator = new WeightCostAllocator();
        assertThrows(IllegalArgumentException.class, () -> byWeightAllocator.allocateCosts(shipment, 1234.56));
    }

    @Test
    void testDuplicates() {
        Shipment shipment1 = new Shipment(
                1,
                List.of(
                        new ShipmentStop(1, "The White House", 0),
                        new ShipmentStop(2, "Bob's House", 425),
                        new ShipmentStop(3, "World of Coca-Cola", 123)
                ),
                List.of(
                        new ShipmentLineItem(1, "tigers", 10, 100, 1, 3),
                        new ShipmentLineItem(1, "lions", 10, 125, 1, 2),
                        new ShipmentLineItem(2, "bears", 5, 150, 2, 3),
                        new ShipmentLineItem(2, "ohmy", 5, 150, 2, 3)  // Duplicate order to bears
                )
        );

        CostAllocator byWeightAllocator = new WeightCostAllocator();
        CostAllocationResult byWeightResult = byWeightAllocator.allocateCosts(shipment1, 1234.34);
        assertEquals(529.00, byWeightResult.costsByOrderId().get(1L), 0.01);
        assertEquals(705.34, byWeightResult.costsByOrderId().get(2L), 0.01);

        Shipment shipment2 = new Shipment(
                1,
                List.of(
                        new ShipmentStop(1, "The White House", 0),
                        new ShipmentStop(2, "Bob's House", 425),
                        new ShipmentStop(3, "World of Coca-Cola", 123),
                        new ShipmentStop(4, "The Zoo", 123)  // Duplicate order to Coca-Cola
                ),
                List.of(
                        new ShipmentLineItem(1, "tigers", 10, 100, 1, 3),
                        new ShipmentLineItem(1, "lions", 10, 125, 1, 2),
                        new ShipmentLineItem(2, "bears", 5, 150, 2, 3)
                )
        );

        byWeightResult = byWeightAllocator.allocateCosts(shipment2, 1234.34);
        assertEquals(740.60, byWeightResult.costsByOrderId().get(1L), 0.01);
        assertEquals(493.74, byWeightResult.costsByOrderId().get(2L), 0.01);
    }
}
