import rs.etf.sab.operations.CityOperations;
import rs.etf.sab.operations.DistrictOperations;
import rs.etf.sab.operations.CourierOperations;
import rs.etf.sab.operations.CourierRequestOperation;
import rs.etf.sab.operations.GeneralOperations;
import rs.etf.sab.operations.UserOperations;
import rs.etf.sab.operations.VehicleOperations;
import rs.etf.sab.operations.PackageOperations;
import rs.etf.sab.tests.TestHandler;
import rs.etf.sab.tests.TestRunner;
import student.mp210336_CityOperations;
import student.mp210336_CourierOperations;
import student.mp210336_CourierRequestOperation;
import student.mp210336_DistrictOperations;
import student.mp210336_GeneralOperations;
import student.mp210336_PackageOperations;
import student.mp210336_UserOperations;
import student.mp210336_VehicleOperations;


public class StudentMain {

    public static void main(String[] args) {
        CityOperations cityOperations = new mp210336_CityOperations(); // Change this to your implementation.
        DistrictOperations districtOperations = new mp210336_DistrictOperations(); // Do it for all classes.
        CourierOperations courierOperations = new mp210336_CourierOperations(); // e.g. = new MyDistrictOperations();
        CourierRequestOperation courierRequestOperation = new mp210336_CourierRequestOperation();
        GeneralOperations generalOperations = new mp210336_GeneralOperations();
        UserOperations userOperations = new mp210336_UserOperations();
        VehicleOperations vehicleOperations = new mp210336_VehicleOperations();
        PackageOperations packageOperations = new mp210336_PackageOperations();

        TestHandler.createInstance(
                cityOperations,
                courierOperations,
                courierRequestOperation,
                districtOperations,
                generalOperations,
                userOperations,
                vehicleOperations,
                packageOperations
        );

        TestRunner.runTests();
    }
}
