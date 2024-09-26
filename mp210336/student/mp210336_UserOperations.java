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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import rs.etf.sab.operations.CourierOperations;
import rs.etf.sab.operations.UserOperations;

/**
 *
 * @author SAB
 */
public class mp210336_UserOperations implements UserOperations {

    @Override
    public boolean insertUser(String userName, String firstName, String lastName, String password) {
        
        String regexPocetnoVeliko = "^[A-Z].*";
        Pattern pattern = Pattern.compile(regexPocetnoVeliko);
        Matcher matcherIme = pattern.matcher(firstName);
        Matcher matcherPrezime = pattern.matcher(lastName);
        
        if (matcherIme.find()==false || matcherPrezime.find()==false) {
            return false;
        }
        
        boolean proveraBroj=false;
        boolean proveraSlovo=false;
        
        for (int i = 0; i < password.length(); i++) {
            if (Character.isLetter(password.charAt(i))) {
                proveraSlovo=true;
            }
            if (Character.isDigit(password.charAt(i))) {
                proveraBroj=true;
            }
            
        }
        
        if(password.length()<8 || proveraSlovo==false || proveraBroj==false)
        {
            return false;
        }
        
        Connection connection=DB.getInstance().getConnection();             
        String provera = "SELECT KorisnickoIme FROM Korisnik WHERE KorisnickoIme=?";      
        String upit = "INSERT INTO Korisnik (Ime,Prezime,KorisnickoIme,Sifra,BrPoslatihPaketa) VALUES(?,?,?,?,0)";
        int ID=-1;
        int broj_redova=0;
        try(
                PreparedStatement proverastmt = connection.prepareStatement(provera,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmt = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            
            proverastmt.setString(1, userName);
            ResultSet proversRS = proverastmt.executeQuery();
            
            if(!proversRS.next())       
            {
                stmt.setString(1, firstName);
                stmt.setString(2, lastName);
                stmt.setString(3, userName);
                stmt.setString(4, password);
                broj_redova = stmt.executeUpdate();
                if(broj_redova>0)
                {
                    ID = 0;
                }
            }
            proversRS.close();
            

        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(ID!=-1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
        

    @Override
    public int declareAdmin(String userName) {
        String upitKorisnik = "SELECT KorisnickoIme FROM Korisnik WHERE KorisnickoIme=?";
        String upitAdmin = "SELECT KorisnickoIme FROM Administrator WHERE KorisnickoIme=?";
        
        int status=0;
        Connection connection=DB.getInstance().getConnection();
        try(PreparedStatement stmt = connection.prepareStatement(upitKorisnik,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmtAdmin = connection.prepareStatement(upitAdmin,PreparedStatement.RETURN_GENERATED_KEYS);     
        )
            {
                stmt.setString(1, userName);
                stmtAdmin.setString(1, userName);
                
                boolean ishod=false;
                
                ResultSet rs = stmt.executeQuery();
                if(!rs.next())
                {
                    status=2;
                }
                rs.close();   
                
                ResultSet rsA = stmtAdmin.executeQuery();
                if(rsA.next() && status!=2)
                {
                    status=1;
                }
                rsA.close();    
                
                
            } catch (SQLException ex) {
                Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        if(status!=0)
        {
            return status;
        }
        
        String upit = "INSERT INTO Administrator (KorisnickoIme) VALUES(?)";
        
        try(PreparedStatement stmt = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);   
        )
            {
                stmt.setString(1, userName);
                stmt.execute();    
            } catch (SQLException ex) {
                Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            }        
        
        
        
        return status;
        
        
    }

    @Override
    public Integer getSentPackages(String... userNames) {
        String upit = "SELECT KorisnickoIme FROM Korisnik WHERE KorisnickoIme=?";
        String upitPaket = "SELECT COUNT(*) as BROJ FROM Paket WHERE KorisnickoIme=?";
        int brojac=0;
        Connection connection=DB.getInstance().getConnection();
        for (String name : userNames) {
            try(PreparedStatement stmt = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmtBroj = connection.prepareStatement(upitPaket,PreparedStatement.RETURN_GENERATED_KEYS);    )
            {
                stmt.setString(1, name);
                boolean ishod=false;
                
                ResultSet rs = stmt.executeQuery();
                if(rs.next())
                {
                    stmtBroj.setString(1, name);
                    ResultSet rsBroj = stmtBroj.executeQuery();
                    if(rsBroj.next())
                    {
                        int brojPaketa = rsBroj.getInt("BROJ");
                        brojac+=brojPaketa;
                    }
                    rsBroj.close();
                }
                else
                {
                    rs.close();
                    return null;
                }
                rs.close();
                
                
            } catch (SQLException ex) {
                Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
        return brojac;
    }

    @Override
    public int deleteUsers(String... userNames) {
        String upitKorisnik = "DELETE FROM Korisnik WHERE KorisnickoIme=?";
        String upitAdmin = "DELETE FROM Administrator WHERE KorisnickoIme=?";
        String upitPaketi = "DELETE FROM Paket WHERE KorisnickoIme=?";
        int brojac=0;
        Connection connection=DB.getInstance().getConnection();
        for (String name : userNames) {
            try(PreparedStatement stmt = connection.prepareStatement(upitKorisnik,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmtAdmin = connection.prepareStatement(upitAdmin,PreparedStatement.RETURN_GENERATED_KEYS);   
                    PreparedStatement stmtPaketi = connection.prepareStatement(upitPaketi,PreparedStatement.RETURN_GENERATED_KEYS); 
                    )
            {
                stmt.setString(1, name);
                stmtAdmin.setString(1, name);
                
                stmtPaketi.setString(1, name);
                
                boolean ishod=false;
                
                stmtPaketi.execute();
                
                CourierOperations kurirOperacije = new mp210336_CourierOperations();
                kurirOperacije.deleteCourier(name);
                
                int brojka = stmtAdmin.executeUpdate();
                int broj = stmt.executeUpdate();
                if(broj>0)
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
    public List<String> getAllUsers() {
        List<String> lista = new ArrayList<>();
        Connection connection=DB.getInstance().getConnection();
        String upit = "SELECT KorisnickoIme FROM Korisnik";
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
    
}
