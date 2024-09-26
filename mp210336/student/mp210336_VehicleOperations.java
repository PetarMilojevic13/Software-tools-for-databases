/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package student;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.VehicleOperations;

/**
 *
 * @author SAB
 */
public class mp210336_VehicleOperations implements VehicleOperations {

    @Override
    public boolean insertVehicle(String licencePlateNumber, int fuelType, BigDecimal fuelConsumtion) {
        
        if(fuelType<0 || fuelType>2)
        {
            return false;
        }
        
        if(fuelConsumtion.compareTo(new BigDecimal(0.0))<=0)
        {
            return false;
        }
        
        Connection connection=DB.getInstance().getConnection();             
        String provera = "SELECT RegistracioniBroj FROM Vozilo WHERE RegistracioniBroj=?";      
        String upit = "INSERT INTO Vozilo (RegistracioniBroj,TipVozila,Potrosnja) VALUES(?,?,?)";
        int ID=-1;
        int broj_redova=0;
        try(
                PreparedStatement proverastmt = connection.prepareStatement(provera,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmt = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            
            proverastmt.setString(1, licencePlateNumber);
            ResultSet proversRS = proverastmt.executeQuery();
            
            if(!proversRS.next())       
            {
                stmt.setString(1, licencePlateNumber);
                stmt.setInt(2, fuelType);
                stmt.setBigDecimal(3, fuelConsumtion);
                broj_redova = stmt.executeUpdate();
                if(broj_redova>0)
                {
                    ID=1;
                }
            }
            proversRS.close();
            

        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(ID==1)
        {
            return true;
        }
        else
        {
            return false;
        }        
    }

    @Override
    public int deleteVehicles(String... licencePlateNumbers) {
        String upit = "DELETE FROM Vozilo WHERE RegistracioniBroj=?";
        String upitKurir = "DELETE FROM ZahtevKurir WHERE RegistracioniBroj=?";
        String upitZahtev = "DELETE FROM Kurir WHERE RegistracioniBroj=?";
        int brojac=0;
        Connection connection=DB.getInstance().getConnection();
        for (String name : licencePlateNumbers) {
            try(PreparedStatement stmt = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);
                  PreparedStatement stmtKurir = connection.prepareStatement(upitKurir,PreparedStatement.RETURN_GENERATED_KEYS);
                    PreparedStatement stmtZahtev = connection.prepareStatement(upitZahtev,PreparedStatement.RETURN_GENERATED_KEYS);
                    )
            {
                stmtZahtev.setString(1, name);
                stmtKurir.setString(1, name);
                
                stmtZahtev.executeUpdate();
                stmtKurir.executeUpdate();
                
                stmt.setString(1, name);
                boolean ishod=false;
                
                int rs =0;
                rs = stmt.executeUpdate();
                if(rs>0)
                {
                    brojac++;
                }
  
            } catch (SQLException ex) {
                Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
        return brojac;        
    }

    @Override
    public List<String> getAllVehichles() {
        List<String> lista = new ArrayList<>();
        Connection connection=DB.getInstance().getConnection();
        String upit = "SELECT RegistracioniBroj FROM Vozilo";
                try(
               PreparedStatement stmt = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            ResultSet rs = stmt.executeQuery();
            
            while(rs.next())
            {
                lista.add(rs.getString("RegistracioniBroj"));
            }
            rs.close();
        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return lista;
    }

    @Override
    public boolean changeFuelType(String licensePlateNumber, int fuelType) {
        if(fuelType<0 || fuelType>2)
        {
            return false;
        }
                Connection connection=DB.getInstance().getConnection();
        String upit = "UPDATE Vozilo SET TipVozila=? WHERE RegistracioniBroj=?";
        int broj_redova=0;
        try(
                PreparedStatement stmt = connection.prepareStatement(upit);)
        {
            
            stmt.setInt(1, fuelType);
            stmt.setString(2, licensePlateNumber);
            broj_redova = stmt.executeUpdate();
        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(broj_redova>0)
        {
            return true;    
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean changeConsumption(String licensePlateNumber, BigDecimal fuelConsumption) {
        
        BigDecimal nulaProvera = new BigDecimal("0.000");
        if(fuelConsumption.compareTo(nulaProvera)<=0)
        {
            return false;
        }
        Connection connection=DB.getInstance().getConnection();
        String upit = "UPDATE Vozilo SET Potrosnja=? WHERE RegistracioniBroj=?";
        int broj_redova=0;
        try(
                PreparedStatement stmt = connection.prepareStatement(upit);)
        {
            stmt.setBigDecimal(1, fuelConsumption);
            stmt.setString(2, licensePlateNumber);

            broj_redova = stmt.executeUpdate();
        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(broj_redova>0)
        {
            return true;    
        }
        else
        {
            return false;
        }     
    }
    
}
