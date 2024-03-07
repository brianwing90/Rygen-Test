import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MileageCostAllocatorTest {

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

        CostAllocator byMilesAllocator = new MileageCostAllocator();
        CostAllocationResult byMilesResult = byMilesAllocator.allocateCosts(shipment, 1234.34);

        assertEquals(1095.82, byMilesResult.costsByOrderId().get(1L), 0.01);
        assertEquals(138.53, byMilesResult.costsByOrderId().get(2L), 0.01);  // Assume order 2 byMiles should be 138.53 when rounded.
    }

    @Test
    void testEmptyShipment() {
        Shipment shipment = new Shipment(
                1,
                List.of(),
                List.of()
        );

        CostAllocator byMilesAllocator = new MileageCostAllocator();
        assertThrows(IllegalArgumentException.class, () -> byMilesAllocator.allocateCosts(shipment, 1234.56));
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

        CostAllocator byMilesAllocator = new MileageCostAllocator();
        assertThrows(IllegalArgumentException.class, () -> byMilesAllocator.allocateCosts(shipment, 1234.56));
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

        CostAllocator byMilesAllocator = new MileageCostAllocator();
        CostAllocationResult byMilesResult = byMilesAllocator.allocateCosts(shipment, 1234.34);

        assertEquals(1234.34, byMilesResult.costsByOrderId().get(1L), 0.01);
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

        CostAllocator byMilesAllocator = new MileageCostAllocator();
        CostAllocationResult byMilesResult = byMilesAllocator.allocateCosts(shipment, 0.0);

        assertEquals(0.0, byMilesResult.costsByOrderId().get(1L), 0.01);
        assertEquals(0.0, byMilesResult.costsByOrderId().get(2L), 0.01);
    }

    @Test
    void testZeroMiles() {
        Shipment shipment = new Shipment(
                1,
                List.of(
                        new ShipmentStop(1, "The White House", 0),
                        new ShipmentStop(2, "Bob's House", 0),
                        new ShipmentStop(3, "World of Coca-Cola", 0)
                ),
                List.of(
                        new ShipmentLineItem(1, "tigers", 10, 100, 1, 3),
                        new ShipmentLineItem(1, "lions", 10, 125, 1, 2),
                        new ShipmentLineItem(2, "bears", 5, 150, 2, 3)
                )
        );

        CostAllocator byMilesAllocator = new MileageCostAllocator();
        assertThrows(IllegalArgumentException.class, () -> byMilesAllocator.allocateCosts(shipment, 1234.56));
    }

    @Test
    void testNegativeMiles() {
        Shipment shipment = new Shipment(
                1,
                List.of(
                        new ShipmentStop(1, "The White House", 0),
                        new ShipmentStop(2, "Bob's House", -425),
                        new ShipmentStop(3, "World of Coca-Cola", -123)
                ),
                List.of(
                        new ShipmentLineItem(1, "tigers", 10, 100, 1, 3),
                        new ShipmentLineItem(1, "lions", 10, 125, 1, 2),
                        new ShipmentLineItem(2, "bears", 5, 150, 2, 3)
                )
        );

        CostAllocator byMilesAllocator = new MileageCostAllocator();
        CostAllocationResult byMilesResult = byMilesAllocator.allocateCosts(shipment, 1234.34);

        assertEquals(1095.82, byMilesResult.costsByOrderId().get(1L), 0.01);
        assertEquals(138.53, byMilesResult.costsByOrderId().get(2L), 0.01);
    }

    @Test
    void testInvalidSequence() {
        // Out-of-sequence shipment.
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
                        new ShipmentLineItem(2, "bears", 5, 150, 3, 2)  // Invalid sequence.
                )
        );

        CostAllocator byMilesAllocator = new MileageCostAllocator();
        assertThrows(IllegalArgumentException.class, () -> byMilesAllocator.allocateCosts(shipment1, 1234.56));

        // Same pickup and dropoff sequence.
        Shipment shipment2 = new Shipment(
                1,
                List.of(
                        new ShipmentStop(1, "The White House", 0),
                        new ShipmentStop(2, "Bob's House", 425),
                        new ShipmentStop(3, "World of Coca-Cola", 123)
                ),
                List.of(
                        new ShipmentLineItem(1, "tigers", 10, 100, 1, 3),
                        new ShipmentLineItem(1, "lions", 10, 125, 1, 2),
                        new ShipmentLineItem(2, "bears", 5, 150, 2, 2)  // Invalid sequence.
                )
        );

        assertThrows(IllegalArgumentException.class, () -> byMilesAllocator.allocateCosts(shipment2, 1234.56));
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

        CostAllocator byMilesAllocator = new MileageCostAllocator();
        CostAllocationResult byMilesResult = byMilesAllocator.allocateCosts(shipment1, 1234.34);

        assertEquals(985.24, byMilesResult.costsByOrderId().get(1L), 0.01);
        assertEquals(249.10, byMilesResult.costsByOrderId().get(2L), 0.01);

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

        byMilesResult = byMilesAllocator.allocateCosts(shipment2, 1234.34);

        assertEquals(1095.82, byMilesResult.costsByOrderId().get(1L), 0.01);
        assertEquals(138.53, byMilesResult.costsByOrderId().get(2L), 0.01);
    }

    @Test
    void testStopsOutOfOrder() {
        Shipment shipment = new Shipment(
                1,
                List.of(
                        new ShipmentStop(2, "Bob's House", 425),
                        new ShipmentStop(3, "World of Coca-Cola", 123),
                        new ShipmentStop(1, "The White House", 0)
                ),
                List.of(
                        new ShipmentLineItem(1, "tigers", 10, 100, 1, 3),
                        new ShipmentLineItem(1, "lions", 10, 125, 1, 2),
                        new ShipmentLineItem(2, "bears", 5, 150, 2, 3)
                )
        );

        CostAllocator byMilesAllocator = new MileageCostAllocator();
        CostAllocationResult byMilesResult = byMilesAllocator.allocateCosts(shipment, 1234.34);

        assertEquals(1095.82, byMilesResult.costsByOrderId().get(1L), 0.01);
        assertEquals(138.53, byMilesResult.costsByOrderId().get(2L), 0.01);  // Assume order 2 byMiles should be 138.53 when rounded.
    }
}
