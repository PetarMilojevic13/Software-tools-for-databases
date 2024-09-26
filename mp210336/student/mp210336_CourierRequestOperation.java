/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.CourierRequestOperation;
import java.sql.CallableStatement;

/**
 *
 * @author SAB
 */
public class mp210336_CourierRequestOperation implements CourierRequestOperation {

    @Override
    public boolean insertCourierRequest(String userName, String licencePlateNumber) {
        Connection connection=DB.getInstance().getConnection();             
        //String proveraPrva = "SELECT KorisnickoIme FROM Kurir WHERE KorisnickoIme=? or RegistracioniBroj=?";   
        String proveraDruga = "SELECT KorisnickoIme FROM ZahtevKurir WHERE KorisnickoIme=?";  
        String proveraTreca = "SELECT KorisnickoIme FROM Korisnik WHERE KorisnickoIme=?"; 
        String proveraCetvrta = "SELECT RegistracioniBroj FROM Vozilo WHERE RegistracioniBroj=?"; 
        String upit = "INSERT INTO ZahtevKurir (KorisnickoIme,RegistracioniBroj) VALUES(?,?)";
        int ID=-1;
        int broj_redova=0;
        try(
           //     PreparedStatement proverastmtPrva = connection.prepareStatement(proveraPrva,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement proverastmtDruga = connection.prepareStatement(proveraDruga,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement proverastmtTreca = connection.prepareStatement(proveraTreca,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement proverastmtCetvrta = connection.prepareStatement(proveraCetvrta,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmt = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            
            //proverastmtPrva.setString(1, userName);
           // proverastmtPrva.setString(2,licencePlateNumber);
            proverastmtDruga.setString(1, userName);
            proverastmtTreca.setString(1, userName);
            proverastmtCetvrta.setString(1,licencePlateNumber);
            
            //ResultSet proversRS1 = proverastmtPrva.executeQuery();
            ResultSet proversRS2 = proverastmtDruga.executeQuery();
            ResultSet proversRS3 = proverastmtTreca.executeQuery();
            ResultSet proversRS4 = proverastmtCetvrta.executeQuery();
            
            //if(!proversRS1.next() && !proversRS2.next() && proversRS3.next() && proversRS4.next())  
            if( !proversRS2.next() && proversRS3.next() && proversRS4.next())
            {
                stmt.setString(1, userName);
                stmt.setString(2, licencePlateNumber);
                broj_redova = stmt.executeUpdate();
                if(broj_redova>0)
                {
                    ID = 1;
                }
            }
           // proversRS1.close();
            proversRS2.close();

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
    public boolean deleteCourierRequest(String userName) {
        Connection connection=DB.getInstance().getConnection();
        String upit = "DELETE FROM ZahtevKurir WHERE KorisnickoIme=?";
        int broj_redova=0;
        try(
                PreparedStatement stmt = connection.prepareStatement(upit);)
        {
            stmt.setString(1, userName);           
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
    public boolean changeVehicleInCourierRequest(String userName, String licencePlateNumber) {
        Connection connection=DB.getInstance().getConnection();
        String proveraVozilo = "SELECT RegistracioniBroj FROM Vozilo WHERE RegistracioniBroj=?";
        String upit = "UPDATE ZahtevKurir SET RegistracioniBroj=? WHERE KorisnickoIme=? ";
        int broj_redova=0;
        try(
                PreparedStatement stmt = connection.prepareStatement(upit);
                PreparedStatement stmtVozilo = connection.prepareStatement(proveraVozilo);
                )
        {
            
            stmtVozilo.setString(1,licencePlateNumber);
            ResultSet rs = stmtVozilo.executeQuery();
            
            if(!rs.next())
            {
                return false;
            }
            
            stmt.setString(1, licencePlateNumber); 
            stmt.setString(2, userName);  
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
    public List<String> getAllCourierRequests() {
        List<String> lista = new ArrayList<>();
        Connection connection=DB.getInstance().getConnection();
        String upit = "SELECT KorisnickoIme FROM ZahtevKurir";
                try(
               PreparedStatement stmt = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            ResultSet rs = stmt.executeQuery();
            
            while(rs.next())
            {
                lista.add(rs.getString("KorisnickoIme"));
            }
            rs.close();
        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return lista;    
    }

    @Override
    public boolean grantRequest(String username) {
        Connection connection=DB.getInstance().getConnection();
        int res=-1;
        String upit = "{? = call postaviKurira(?)}";
        
                try(
               CallableStatement stmt = connection.prepareCall(upit);)
        {
            stmt.registerOutParameter(1, java.sql.Types.INTEGER);
            stmt.setString(2, username);
            stmt.execute();
            res = stmt.getInt(1);
            
        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        if(res==0)
        {
            return true;
        }
        else
        {
            return false;
        }
              
    }
    
}
