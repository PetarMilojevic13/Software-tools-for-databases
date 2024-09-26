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
import rs.etf.sab.operations.CourierOperations;
import rs.etf.sab.operations.CourierRequestOperation;
import rs.etf.sab.operations.DistrictOperations;

/**
 *
 * @author SAB
 */
public class mp210336_CourierOperations implements CourierOperations {

    @Override
    public boolean insertCourier(String courierUserName, String licencePlateNumber) {
        Connection connection=DB.getInstance().getConnection();             
        String provera = "SELECT KorisnickoIme FROM Kurir WHERE KorisnickoIme=? or RegistracioniBroj=?"; 
        String proveraKorisnik = "SELECT KorisnickoIme FROM Korisnik WHERE KorisnickoIme=?"; 
        String proveraVozilo = "SELECT RegistracioniBroj FROM Vozilo WHERE RegistracioniBroj=?";
        String upit = "INSERT INTO Kurir (KorisnickoIme,RegistracioniBroj,Status,Profit,BrojIsporucenihPaketa) VALUES(?,?,0,0.000,0)";
        int ID=-1;
        int broj_redova=0;
        try(
                PreparedStatement proverastmt = connection.prepareStatement(provera,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement proverastmtKorisnik = connection.prepareStatement(proveraKorisnik,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement proverastmtVozilo = connection.prepareStatement(proveraVozilo,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmt = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            
            proverastmt.setString(1, courierUserName);
            proverastmt.setString(2, licencePlateNumber);
            ResultSet proversRS = proverastmt.executeQuery();
            
            proverastmtKorisnik.setString(1, courierUserName);
            proverastmtVozilo.setString(1, licencePlateNumber);
            
            ResultSet proveraRSKorisnik = proverastmtKorisnik.executeQuery();
            ResultSet proveraRSVozilo = proverastmtVozilo.executeQuery();
            
            if(!proversRS.next() && proveraRSKorisnik.next() && proveraRSVozilo.next())       
            {
                stmt.setString(1, courierUserName);
                stmt.setString(2, licencePlateNumber);
                broj_redova = stmt.executeUpdate();
                if(broj_redova>0)
                {
                    ID = 1;
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
    public boolean deleteCourier(String courierUserName) {
        Connection connection=DB.getInstance().getConnection();
        String upitPonude = "DELETE FROM Ponuda WHERE KorisnickoIme=?";
        String upit = "DELETE FROM Kurir WHERE KorisnickoIme=?";
        int broj_redova=0;
        try(
                PreparedStatement stmtPonuda = connection.prepareStatement(upitPonude);
                PreparedStatement stmt = connection.prepareStatement(upit);)
        {
            stmt.setString(1, courierUserName);
            
            stmtPonuda.setString(1,courierUserName);
            stmtPonuda.execute();
            
            
            CourierRequestOperation pomocni = new mp210336_CourierRequestOperation();
            
            boolean i = pomocni.deleteCourierRequest(courierUserName);
            
            
            
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
    public List<String> getCouriersWithStatus(int statusOfCourier) {
        List<String> lista = new ArrayList<>();
        Connection connection=DB.getInstance().getConnection();
        String upit = "SELECT KorisnickoIme FROM Kurir WHERE Status=?";
                try(
               PreparedStatement stmt = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            stmt.setInt(1, statusOfCourier);
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
    public List<String> getAllCouriers() {
        List<String> lista = new ArrayList<>();
        Connection connection=DB.getInstance().getConnection();
        String upit = "SELECT KorisnickoIme FROM Kurir ORDER BY Profit DESC";
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
    public BigDecimal getAverageCourierProfit(int numberOfDeliveries) {
        List<BigDecimal> lista = new ArrayList<>();
        Connection connection=DB.getInstance().getConnection();
        String upit = "SELECT KorisnickoIme,Profit FROM Kurir WHERE BrojIsporucenihPaketa>=?";
                try(
               PreparedStatement stmt = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            stmt.setInt(1, numberOfDeliveries);
            ResultSet rs = stmt.executeQuery();
            
            while(rs.next())
            {
                lista.add(rs.getBigDecimal("Profit"));
            }
            rs.close();
        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
                
        BigDecimal suma = new BigDecimal("0.000");
        BigDecimal brojac = new BigDecimal(lista.size());
        if(lista.size()==0)
        {
            return suma;
        }
        else
        {
            for (int i = 0; i < lista.size(); i++) {
                BigDecimal brojka = lista.get(i);
                suma = suma.add(brojka);
            }
            
            BigDecimal prosek = suma.divide(brojac);
            return prosek;
        }
    }
    
}
