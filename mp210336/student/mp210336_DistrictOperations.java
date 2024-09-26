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
import rs.etf.sab.operations.DistrictOperations;

/**
 *
 * @author SAB
 */
public class mp210336_DistrictOperations implements DistrictOperations {

    @Override
    public int insertDistrict(String name, int cityId, int xCord, int yCord) {
        Connection connection=DB.getInstance().getConnection();             
        String provera = "SELECT IdOps FROM Opstina WHERE Naziv=?"; 
        String proveraGrad = "SELECT IdGra FROM Grad WHERE IdGra=?"; 
        String upit = "INSERT INTO Opstina (IdGra,Xkoord,Ykoord,Naziv) VALUES(?,?,?,?)";
        int ID=-1;
        int broj_redova=0;
        try(
                PreparedStatement stmtGrad = connection.prepareStatement(proveraGrad,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement proverastmt = connection.prepareStatement(provera,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmt = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            
            proverastmt.setString(1, name);
            
            stmtGrad.setInt(1,cityId);
            
            ResultSet proversRS = proverastmt.executeQuery();
            
            ResultSet proversGrad = stmtGrad.executeQuery();
            
            if(!proversRS.next() && proversGrad.next())       
            {
                stmt.setInt(1, cityId);
                stmt.setInt(2, xCord);
                stmt.setInt(3, yCord);
                stmt.setString(4, name);
                
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
    public int deleteDistricts(String... names) {
        String upit = "SELECT IdOps FROM Opstina WHERE Naziv=?";
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
                    int ID = rs.getInt("IdOps");
                    
                    ishod = deleteDistrict(ID);
                    
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
    public boolean deleteDistrict(int idDistrict)
    {
        Connection connection=DB.getInstance().getConnection();
        String upit = "DELETE FROM Opstina WHERE IdOps=?";
        String upitBrisanjePaketa = "DELETE FROM Paket WHERE OpstinaPreuzimanje=? or OpstinaDostava=?";
        int broj_redova=0;
        try(
                PreparedStatement stmtPaketi = connection.prepareStatement(upitBrisanjePaketa);
                PreparedStatement stmt = connection.prepareStatement(upit);)
        {
            stmtPaketi.setInt(1,idDistrict);
            stmtPaketi.setInt(2,idDistrict);
            stmtPaketi.execute();
            
            stmt.setInt(1, idDistrict);
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
    public int deleteAllDistrictsFromCity(String nameOfTheCity) {
             
        String upit = "SELECT IdGra FROM Grad WHERE Grad.Naziv=?";
        Connection connection=DB.getInstance().getConnection(); 
        int IdGra;
        int broj_redova=0;        
        
        String upitBrisanje = "DELETE FROM Opstina WHERE Opstina.IdGra=?";
        
        try(
                PreparedStatement stmt = connection.prepareStatement(upit);
                PreparedStatement deleteStmt = connection.prepareStatement(upitBrisanje);)
        {
            stmt.setString(1, nameOfTheCity);

            boolean ishod=false;

            ResultSet rs = stmt.executeQuery();
            if(rs.next())
            {
                IdGra = rs.getInt("IdGra");

                List<Integer> lista = getAllDistrictsFromCity(IdGra);
                
                for (int i = 0; i < lista.size(); i++) {
                    Integer IdOps = lista.get(i);
                    ishod = false;
                    ishod = deleteDistrict(IdOps);
                    if(ishod==true)
                    {
                        broj_redova++;
                    }
                }
            }
            rs.close();
            
            
            
        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return broj_redova;
    }

    @Override
    public List<Integer> getAllDistrictsFromCity(int idCity) {
        List<Integer> lista = new ArrayList<>();
        Connection connection=DB.getInstance().getConnection();
        String upit = "SELECT IdOps FROM Opstina,Grad WHERE Grad.IdGra=? AND Opstina.IdGra=Grad.IdGra";
                try(
               PreparedStatement stmt = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            stmt.setInt(1, idCity);
            ResultSet rs = stmt.executeQuery();
            
            while(rs.next())
            {
                lista.add(rs.getInt("IdOps"));
            }
            rs.close();
        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return lista;  
    }

    @Override
    public List<Integer> getAllDistricts() {
        List<Integer> lista = new ArrayList<>();
        Connection connection=DB.getInstance().getConnection();
        String upit = "SELECT IdOps FROM Opstina";
                try(
               PreparedStatement stmt = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            ResultSet rs = stmt.executeQuery();
            
            while(rs.next())
            {
                lista.add(rs.getInt("IdOps"));
            }
            rs.close();
        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return lista;  
    }
    
}
