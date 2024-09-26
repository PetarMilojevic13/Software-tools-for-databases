/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.GeneralOperations;
import java.sql.Statement;
/**
 *
 * @author SAB
 */
public class mp210336_GeneralOperations implements GeneralOperations {

    @Override
    public void eraseAll() {
        Connection connection=DB.getInstance().getConnection();                  
        String upit = "";
        int ID=-1;
        int broj_redova=0;
        try(
                Statement stmt = connection.createStatement();)
        {

            stmt.executeUpdate("DELETE FROM VoznjaZahtev where 1=1"); 
            stmt.executeUpdate("DELETE FROM Voznja where 1=1");
            stmt.executeUpdate("DELETE FROM ZahtevPrevoz where 1=1");  
            stmt.executeUpdate("DELETE FROM Ponuda where 1=1"); 
            stmt.executeUpdate("DELETE FROM Paket where 1=1");             
            stmt.executeUpdate("DELETE FROM Opstina where 1=1");            
            stmt.executeUpdate("DELETE FROM Grad where 1=1");            
            stmt.executeUpdate("DELETE FROM ZahtevKurir where 1=1"); 
            stmt.executeUpdate("DELETE FROM Kurir where 1=1");
            stmt.executeUpdate("DELETE FROM Vozilo where 1=1");              
            stmt.executeUpdate("DELETE FROM Administrator where 1=1");
            stmt.executeUpdate("DELETE FROM Korisnik where 1=1");
        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
