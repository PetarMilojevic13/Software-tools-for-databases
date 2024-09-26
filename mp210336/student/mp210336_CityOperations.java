/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package student;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.CityOperations;



import java.util.List;
import java.util.Random;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rs.etf.sab.operations.CityOperations;
import rs.etf.sab.operations.CourierOperations;
import rs.etf.sab.operations.CourierRequestOperation;
import rs.etf.sab.operations.DistrictOperations;
import rs.etf.sab.operations.GeneralOperations;
import rs.etf.sab.operations.VehicleOperations;



/**
 *
 * @author SAB
 */
public class mp210336_CityOperations implements CityOperations {
    
   

    @Override
    public int insertCity(String name,String postalCode) {
        Connection connection=DB.getInstance().getConnection();             
        String provera = "SELECT IdGra FROM Grad WHERE Naziv=? or PostanskiBroj=?";      
        String upit = "INSERT INTO Grad (Naziv,PostanskiBroj) VALUES(?,?)";
        int ID=-1;
        int broj_redova=0;
        try(
                PreparedStatement proverastmt = connection.prepareStatement(provera,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmt = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            
            proverastmt.setString(1, name);
            proverastmt.setString(2, postalCode);
            ResultSet proversRS = proverastmt.executeQuery();
            
            if(!proversRS.next())       
            {
                stmt.setString(1, name);
                stmt.setString(2, postalCode);
                broj_redova = stmt.executeUpdate();
                if(broj_redova>0)
                {
                    ResultSet rs = stmt.getGeneratedKeys();
                    rs.next();
                    ID = rs.getInt(1);
                    rs.close();
                }
            }
            proversRS.close();
            

        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return ID;
    }

    @Override
    public int deleteCity(String... names) {
        String upit = "SELECT IdGra FROM Grad WHERE Naziv=?";
        int brojac=0;
        Connection connection=DB.getInstance().getConnection();
        for (String name : names) {
            try(PreparedStatement stmt = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);)
            {
                stmt.setString(1, name);
                boolean ishod=false;
                
                ResultSet rs = stmt.executeQuery();
                if(rs.next())
                {
                    int ID = rs.getInt("IdGra");
                    
                    ishod = deleteCity(ID);
                    
                    if(ishod==true)
                    {
                        brojac++;
                    }
                }
                rs.close();
                
                
            } catch (SQLException ex) {
                Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
        return brojac;
    }

    @Override
    public boolean deleteCity(int idCity) {
        Connection connection=DB.getInstance().getConnection();
        String upit = "DELETE FROM Grad WHERE IdGra=?";
        int broj_redova=0;
        try(
                PreparedStatement stmt = connection.prepareStatement(upit);)
        {
            stmt.setInt(1, idCity);
            
            
            DistrictOperations pomocni = new mp210336_DistrictOperations();
            
            List<Integer> lista = pomocni.getAllDistrictsFromCity(idCity);
            
            for (int i = 0; i < lista.size(); i++) {
                Integer brojka = lista.get(i);
                pomocni.deleteDistrict(brojka);
            }
            
            
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
    public List<Integer> getAllCities() {
        List<Integer> lista = new ArrayList<>();
        Connection connection=DB.getInstance().getConnection();
        String upit = "SELECT IdGra FROM Grad";
                try(
               PreparedStatement stmt = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            ResultSet rs = stmt.executeQuery();
            
            while(rs.next())
            {
                lista.add(rs.getInt("IdGra"));
            }
            rs.close();
        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return lista;     
    }


}
