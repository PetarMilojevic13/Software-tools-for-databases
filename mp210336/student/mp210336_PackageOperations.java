/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package student;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import rs.etf.sab.operations.DistrictOperations;
import rs.etf.sab.operations.PackageOperations;

/**
 *
 * @author SAB
 */
public class mp210336_PackageOperations implements PackageOperations {
    
    
    private static final BigDecimal[] POCETNA_CENA = {new BigDecimal("10"),new BigDecimal("25"),new BigDecimal("75")};
    private static final BigDecimal[] TEZINSKI_FAKTOR = {new BigDecimal("0"),new BigDecimal("1"),new BigDecimal("2")};
    private static final BigDecimal[] CENA_KG = {new BigDecimal("0"),new BigDecimal("100"),new BigDecimal("300")};
    private static final BigDecimal[] CENA_GORIVA = {new BigDecimal("15"),new BigDecimal("32"),new BigDecimal("36")};
    
    public class Par implements Pair<Integer, BigDecimal> {
        Integer offerId;
        BigDecimal procenat;

        public Par(Integer offerId, BigDecimal procenat) {
            this.offerId = offerId;
            this.procenat = procenat;
        }
        
        public Integer getFirstParam()
        {
            return offerId;
        };

        public BigDecimal getSecondParam()
        {
            return procenat;
        };
    
    public boolean equals(Pair a, Pair b) {
      return (a.getFirstParam().equals(b.getFirstParam()) && a.getSecondParam().equals(b.getSecondParam()));
    }
  }
    
    public BigDecimal nadjiNovuCenu(int packageId)
    {

        int XkoordFrom;
        int YkoordFrom;
        int XkoordTo;
        int YkoordTo;
        int packageType;
        BigDecimal distanca=null;
        BigDecimal cena = null;
        BigDecimal weight=null;
        Connection connection=DB.getInstance().getConnection();             
        String proveraOpstinaTo = "SELECT IdOps,Xkoord,Ykoord,TipPaketa,TezinaPaketa FROM Opstina,Paket WHERE Opstina.IdOps=Paket.OpstinaDostava and Paket.IdPak=?"; 
        String proveraOpstinaFrom = "SELECT IdOps,Xkoord,Ykoord FROM Opstina,Paket WHERE Opstina.IdOps=Paket.OpstinaPreuzimanje and Paket.IdPak=?";    
        String proveraProcenat = "SELECT Procenat FROM ZahtevPrevoz WHERE ZahtevPrevoz.IdPak=? and ZahtevPrevoz.Vreme IS NOT NULL"; 
        try(
                PreparedStatement stmtProcenat = connection.prepareStatement(proveraProcenat,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmtOpstinaTo = connection.prepareStatement(proveraOpstinaTo,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmtOpstinaFrom = connection.prepareStatement(proveraOpstinaFrom,PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            
            stmtProcenat.setInt(1, packageId);
            stmtOpstinaTo.setInt(1, packageId);
            stmtOpstinaFrom.setInt(1, packageId);
            
            ResultSet RSProcenat = stmtProcenat.executeQuery();
            ResultSet RSOpstinaTo = stmtOpstinaTo.executeQuery();
            ResultSet RSOpstinaFrom = stmtOpstinaFrom.executeQuery();
            
            if(!RSProcenat.next() || !RSOpstinaTo.next() || !RSOpstinaFrom.next())       
            {
                RSProcenat.close();
                RSOpstinaTo.close();
                RSOpstinaFrom.close();
                return null;
            }
            
            XkoordFrom=RSOpstinaFrom.getInt("Xkoord");
            YkoordFrom=RSOpstinaFrom.getInt("Ykoord");
            XkoordTo=RSOpstinaTo.getInt("Xkoord");
            YkoordTo=RSOpstinaTo.getInt("Ykoord");
            packageType = RSOpstinaTo.getInt("TipPaketa");
            weight = RSOpstinaTo.getBigDecimal("TezinaPaketa");
            
            BigDecimal procenat = RSProcenat.getBigDecimal("Procenat");
            
            
            int deltaX = XkoordTo - XkoordFrom;
            int deltaY = YkoordTo - YkoordFrom;

            BigDecimal deltaXKvadrat = BigDecimal.valueOf(deltaX).pow(2);
            BigDecimal deltaYKvadrat  = BigDecimal.valueOf(deltaY).pow(2);
            BigDecimal sumaKvadrata = deltaXKvadrat.add(deltaYKvadrat);
            
            double dist = Math.sqrt(deltaX*deltaX+deltaY*deltaY);
           // distanca = sumaKvadrata.sqrt(new MathContext(20, RoundingMode.HALF_UP));

           // distanca =  distanca.setScale(3, RoundingMode.HALF_UP);
            
            cena = TEZINSKI_FAKTOR[packageType];
            cena = cena.multiply(weight);
            cena = cena.multiply(CENA_KG[packageType]);            
            cena = cena.add(POCETNA_CENA[packageType]);
            cena = cena.multiply(new BigDecimal(dist));  
            
            procenat = procenat.divide(new BigDecimal(100));
            
            cena = cena.multiply(procenat.add(new BigDecimal(1)));
            

            RSProcenat.close();
            RSOpstinaTo.close();
            RSOpstinaFrom.close();
        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        return cena;
    }

    @Override
    public int insertPackage(int districtFrom, int districtTo, String userName, int packageType, BigDecimal weight) {
        
       BigDecimal nula = new BigDecimal("0.000");
        if (weight.compareTo(nula)<=0) {
            return -1;
        }
        
        if(packageType<0 || packageType>2)
        {
            return -1;
        }
        
        int XkoordFrom;
        int YkoordFrom;
        int XkoordTo;
        int YkoordTo;
        BigDecimal distanca = new BigDecimal("0.000");
        double dist=0;
        
        Connection connection=DB.getInstance().getConnection();             
        String proveraKorisnik = "SELECT KorisnickoIme FROM Korisnik WHERE KorisnickoIme=?";
        String proveraOpstinaTo = "SELECT IdOps,Xkoord,Ykoord FROM Opstina WHERE IdOps=?"; 
        String proveraOpstinaFrom = "SELECT IdOps,Xkoord,Ykoord FROM Opstina WHERE IdOps=?"; 
        
        try(
                PreparedStatement stmtKorisnik = connection.prepareStatement(proveraKorisnik,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmtOpstinaTo = connection.prepareStatement(proveraOpstinaTo,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmtOpstinaFrom = connection.prepareStatement(proveraOpstinaFrom,PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            
            stmtKorisnik.setString(1, userName);
            stmtOpstinaTo.setInt(1, districtTo);
            stmtOpstinaFrom.setInt(1, districtFrom);
            
            ResultSet RSKorisnik = stmtKorisnik.executeQuery();
            ResultSet RSOpstinaTo = stmtOpstinaTo.executeQuery();
            ResultSet RSOpstinaFrom = stmtOpstinaFrom.executeQuery();
            
            if(!RSKorisnik.next() || !RSOpstinaTo.next() || !RSOpstinaFrom.next())       
            {
                RSKorisnik.close();
                RSOpstinaTo.close();
                RSOpstinaFrom.close();
                return -1;
            }
            
            XkoordFrom=RSOpstinaFrom.getInt("Xkoord");
            YkoordFrom=RSOpstinaFrom.getInt("Ykoord");
            XkoordTo=RSOpstinaTo.getInt("Xkoord");
            YkoordTo=RSOpstinaTo.getInt("Ykoord");
            
            
            int deltaX = XkoordTo - XkoordFrom;
            int deltaY = YkoordTo - YkoordFrom;
            
            dist = Math.sqrt(deltaX*deltaX+deltaY*deltaY);

           // BigDecimal deltaXKvadrat = BigDecimal.valueOf(deltaX).pow(2);
           // BigDecimal deltaYKvadrat  = BigDecimal.valueOf(deltaY).pow(2);
           // BigDecimal sumaKvadrata = deltaXKvadrat.add(deltaYKvadrat);
            
            
           // distanca = sumaKvadrata.sqrt(new MathContext(20, RoundingMode.HALF_UP));

            //distanca =  distanca.setScale(3, RoundingMode.HALF_UP);

            RSKorisnik.close();
            RSOpstinaTo.close();
            RSOpstinaFrom.close();
        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }        
        
        

        String upitPaket = "INSERT INTO Paket (OpstinaPreuzimanje,OpstinaDostava,TipPaketa,TezinaPaketa,KorisnickoIme) VALUES(?,?,?,?,?)";
        String upitZahtev = "INSERT INTO ZahtevPrevoz (IdPak,KorisnickoIme,Status,Cena,Vreme,Procenat) VALUES(?,NULL,0,?,NULL,NULL)";
        int ID=-1;
        int broj_redova;
        try(
                PreparedStatement stmtPaket = connection.prepareStatement(upitPaket,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmtZahtev = connection.prepareStatement(upitZahtev,PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            
            stmtPaket.setInt(1, districtFrom);
            stmtPaket.setInt(2, districtTo);
            stmtPaket.setInt(3, packageType);
            stmtPaket.setBigDecimal(4, weight);
            stmtPaket.setString(5, userName);

            broj_redova = stmtPaket.executeUpdate();
            if(broj_redova>0)
            {
                ResultSet rs = stmtPaket.getGeneratedKeys();
                rs.next();
                ID = rs.getInt(1);
                rs.close();
            }
            BigDecimal cena = TEZINSKI_FAKTOR[packageType];
            cena = cena.multiply(weight);
            cena = cena.multiply(CENA_KG[packageType]);            
            cena = cena.add(POCETNA_CENA[packageType]);
            cena = cena.multiply(new BigDecimal(dist));  
            
            
            
            stmtZahtev.setInt(1, ID);
            stmtZahtev.setBigDecimal(2, cena);
            broj_redova = stmtZahtev.executeUpdate();
       
        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }  
        
        return ID;
        
    }

    @Override
    public int insertTransportOffer(String couriersUserName, int packageId, BigDecimal pricePercentage) {     
        if (pricePercentage == null) {
            Random random = new Random();
            //Napravljeno je da ide u opsegu od 8% do 12%
            double randomPercentage = 4 * random.nextDouble()+8;
            pricePercentage = BigDecimal.valueOf(randomPercentage).setScale(3, RoundingMode.HALF_UP);
        }
        
        
        Connection connection=DB.getInstance().getConnection();             
        String proveraKurir = "SELECT KorisnickoIme FROM Kurir WHERE KorisnickoIme=? and Status=0";     
        String proveraPaket = "SELECT Paket.IdPak FROM Paket,ZahtevPrevoz WHERE Paket.IdPak=? and Paket.IdPak=ZahtevPrevoz.IdPak and ZahtevPrevoz.Vreme IS NULL";   
        String upit = "INSERT INTO Ponuda (IdPak,KorisnickoIme,Procenat) VALUES(?,?,?)";
        int ID=-1;
        int broj_redova=0;
        try(
                PreparedStatement stmtKurir = connection.prepareStatement(proveraKurir,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmtPaket = connection.prepareStatement(proveraPaket,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmt = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            
            stmtKurir.setString(1, couriersUserName);
            stmtPaket.setInt(1, packageId);
            
            ResultSet RSKorisnik = stmtKurir.executeQuery();
            ResultSet RSPaket = stmtPaket.executeQuery();
            if(RSKorisnik.next() && RSPaket.next())       
            {
                stmt.setInt(1, packageId);
                stmt.setString(2, couriersUserName);
                stmt.setBigDecimal(3, pricePercentage);
                broj_redova = stmt.executeUpdate();
                
                if(broj_redova>0)
                {
                    ResultSet rs = stmt.getGeneratedKeys();
                    rs.next();
                    ID = rs.getInt(1);
                    rs.close();
                }
            }
            else
            {
                RSKorisnik.close();
                RSPaket.close();  
                return -1;
            }
            RSKorisnik.close();
            RSPaket.close();

        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return ID;
    }

    @Override
    public boolean acceptAnOffer(int offerId) {
  
        Connection connection=DB.getInstance().getConnection();             
        String proveraPonuda = "SELECT Ponuda.IdPon,Ponuda.IdPak,Ponuda.Procenat,Ponuda.KorisnickoIme FROM Ponuda,Kurir,ZahtevPrevoz WHERE IdPon=? and Ponuda.KorisnickoIme=Kurir.KorisnickoIme and Kurir.Status=0 and ZahtevPrevoz.IdPak=Ponuda.IdPak and ZahtevPrevoz.Vreme IS NULL";  
        //String updateKorisnikBrPoslatih = "UPDATE Korisnik SET BrPoslatihPaketa=BrPoslatihPaketa+1 WHERE ";
        String upit = "UPDATE ZahtevPrevoz Set Vreme=?,KorisnickoIme=?,Status=1,Procenat=? WHERE IdPak=?";
        String upitCena = "UPDATE ZahtevPrevoz Set Cena=? WHERE IdPak=?";
        int ID=-1;
        boolean ishod=false;
        int broj_redova=0;
        try(
                PreparedStatement stmtProvera = connection.prepareStatement(proveraPonuda,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmt = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmtCena = connection.prepareStatement(upitCena,PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            
            stmtProvera.setInt(1, offerId);
            
            ResultSet RSProvera = stmtProvera.executeQuery();
            if(RSProvera.next())       
            {
                int IdPak = RSProvera.getInt(2);
                double procenat = RSProvera.getBigDecimal(3).doubleValue();
                String korisnickoIme = RSProvera.getString(4);
                
                LocalDate localDate = LocalDate.now();


                Date vreme = Date.valueOf(localDate);
                
                stmt.setDate(1, vreme);
                stmt.setString(2, korisnickoIme);
                stmt.setBigDecimal(3, new BigDecimal(procenat));
                stmt.setInt(4, IdPak);
                
                broj_redova = stmt.executeUpdate();
                
                if(broj_redova>0)
                {
                    ishod=true;
                }
                
                BigDecimal cena = nadjiNovuCenu(IdPak);
                
                stmtCena.setBigDecimal(1, cena);
                stmtCena.setInt(2, IdPak);
                broj_redova = stmtCena.executeUpdate();
            }
            else
            { 
                RSProvera.close();
                return false;
            }
            RSProvera.close();
        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return ishod;        
    }

    @Override
    public List<Integer> getAllOffers() {
        List<Integer> lista = new ArrayList<>();
        Connection connection=DB.getInstance().getConnection();
        String upit = "SELECT IdPon FROM Ponuda";
                try(
               PreparedStatement stmt = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            ResultSet rs = stmt.executeQuery();
            
            while(rs.next())
            {
                lista.add(rs.getInt("IdPon"));
            }
            rs.close();
        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return lista; 
    }

    @Override
    public List<Pair<Integer, BigDecimal>> getAllOffersForPackage(int packageId) {
        List<Pair<Integer, BigDecimal>> lista = new ArrayList<>();
        Connection connection=DB.getInstance().getConnection();
        String upit = "SELECT IdPon,Procenat FROM Ponuda WHERE Ponuda.IdPak=?";
                try(
               PreparedStatement stmt = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);
                        )
        {
            stmt.setInt(1, packageId);
            ResultSet rs = stmt.executeQuery();
            
            while(rs.next())
            {
                int IdPon = rs.getInt("IdPon");
                BigDecimal procenat = rs.getBigDecimal("Procenat");
                Par par = new Par(IdPon,procenat);
                lista.add(par);
            }
            rs.close();
        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return lista;        
    }

    @Override
    public boolean deletePackage(int packageId) {
        Connection connection=DB.getInstance().getConnection();
        String upit = "DELETE FROM Paket WHERE IdPak=?";
        String upitZahtev = "DELETE FROM ZahtevPrevoz WHERE IdPak=?";
        String upitPonuda = "DELETE FROM Ponuda WHERE IdPak=?";
        int broj_redova=0;
        int broj_redova_res=0;
        try(
                PreparedStatement stmt = connection.prepareStatement(upit);
                PreparedStatement stmtPonuda = connection.prepareStatement(upitPonuda);
                PreparedStatement stmtZahtev = connection.prepareStatement(upitZahtev);
                )
        {
            stmt.setInt(1, packageId);
            stmtPonuda.setInt(1, packageId);
            stmtZahtev.setInt(1, packageId);
   
            broj_redova = stmtZahtev.executeUpdate();
            broj_redova = stmtPonuda.executeUpdate();
            broj_redova_res = stmt.executeUpdate();
        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(broj_redova_res>0)
        {
            return true;    
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean changeWeight(int packageId,BigDecimal newWeight) {
        
        
        BigDecimal nula = new BigDecimal("0.000");
        if (newWeight.compareTo(nula)<=0) {
            return false;
        }
        
        
        String upitProvera = "SELECT IdPak FROM Paket WHERE IdPak=?";
        String upitUpdatePaket = "UPDATE Paket SET TezinaPaketa=? WHERE IdPak=?";
        String upitUpdateZahtev = "UPDATE ZahtevPrevoz SET Cena=? WHERE IdPak=?";
        int tipPaketa=0;
        boolean ishod = false;
        Connection connection=DB.getInstance().getConnection();
            try(PreparedStatement stmtProvera = connection.prepareStatement(upitProvera,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmtPaket = connection.prepareStatement(upitUpdatePaket,PreparedStatement.RETURN_GENERATED_KEYS);  
                PreparedStatement stmtZahtev = connection.prepareStatement(upitUpdateZahtev,PreparedStatement.RETURN_GENERATED_KEYS); )
            {
                stmtProvera.setInt(1, packageId);
                ResultSet rs = stmtProvera.executeQuery();
                if(rs.next())
                {
                    stmtPaket.setBigDecimal(1, newWeight);
                    stmtPaket.setInt(2,packageId);
                    int proverica = stmtPaket.executeUpdate();
                    if(proverica>0)
                    {
                        ishod=true;
                        BigDecimal cena = nadjiNovuCenu(packageId);
                        stmtZahtev.setBigDecimal(1, cena);
                        stmtZahtev.setInt(2,packageId);
                        proverica=stmtZahtev.executeUpdate();
                    }
                }
                else
                {
                    rs.close();
                    return false;
                }
                rs.close();
                
                
            } catch (SQLException ex) {
                Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
         
            return ishod;        
    }

    @Override
    public boolean changeType(int packageId,int newType) {
        
        if(newType<0 || newType>2)
        {
            return false;
        }
        
        String upitProvera = "SELECT IdPak FROM Paket WHERE IdPak=?";
        String upitUpdatePaket = "UPDATE Paket SET TipPaketa=? WHERE IdPak=?";
        String upitUpdateZahtev = "UPDATE ZahtevPrevoz SET Cena=? WHERE IdPak=?";
        int tipPaketa=0;
        boolean ishod = false;
        Connection connection=DB.getInstance().getConnection();
            try(PreparedStatement stmtProvera = connection.prepareStatement(upitProvera,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmtPaket = connection.prepareStatement(upitUpdatePaket,PreparedStatement.RETURN_GENERATED_KEYS);  
                PreparedStatement stmtZahtev = connection.prepareStatement(upitUpdateZahtev,PreparedStatement.RETURN_GENERATED_KEYS); )
            {
                stmtProvera.setInt(1, packageId);
                ResultSet rs = stmtProvera.executeQuery();
                if(rs.next())
                {
                    stmtPaket.setInt(1, newType);
                    stmtPaket.setInt(2,packageId);
                    int proverica = stmtPaket.executeUpdate();
                    if(proverica>0)
                    {
                        ishod=true;
                        BigDecimal cena = nadjiNovuCenu(packageId);
                        stmtZahtev.setBigDecimal(1, cena);
                        stmtZahtev.setInt(2,packageId);
                        proverica=stmtZahtev.executeUpdate();
                    }
                }
                else
                {
                    rs.close();
                    return false;
                }
                rs.close();
                
                
            } catch (SQLException ex) {
                Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
         
            return ishod;
    }

    @Override
    public Integer getDeliveryStatus(int packageId) {
        String upit = "SELECT IdPak FROM Paket WHERE IdPak=?";
        String upitStatus = "SELECT Status FROM ZahtevPrevoz WHERE IdPak=?";
        int tipPaketa=0;
        Connection connection=DB.getInstance().getConnection();
            try(PreparedStatement stmtProvera = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmt = connection.prepareStatement(upitStatus,PreparedStatement.RETURN_GENERATED_KEYS);    )
            {
                stmtProvera.setInt(1, packageId);
                boolean ishod=false;
                
                ResultSet rs = stmtProvera.executeQuery();
                if(rs.next())
                {
                    stmt.setInt(1, packageId);
                    ResultSet rsBroj = stmt.executeQuery();
                    if(rsBroj.next())
                    {
                       tipPaketa = rsBroj.getInt("Status");
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
         
            return tipPaketa;
    }

    @Override
    public BigDecimal getPriceOfDelivery(int packageId) {
        if(getAcceptanceTime(packageId)==null)
        {
            return null;
        }
        BigDecimal cena = null;
        String upit = "SELECT Cena FROM ZahtevPrevoz WHERE IdPak=? and Vreme IS NOT NULL";
        int tipPaketa=0;
        Connection connection=DB.getInstance().getConnection();
            try(PreparedStatement stmtProvera = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);
                )
            {
                stmtProvera.setInt(1, packageId);
                boolean ishod=false;
                
                ResultSet rs = stmtProvera.executeQuery();
                if(rs.next())
                {
                    cena=rs.getBigDecimal("Cena");
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
         
            return cena; 
    }

    @Override
    public Date getAcceptanceTime(int packageId) {
        Date datum = null;
        String upit = "SELECT IdPak FROM Paket WHERE IdPak=?";
        String upitStatus = "SELECT Vreme FROM ZahtevPrevoz WHERE IdPak=? and Vreme IS NOT NULL";
        int tipPaketa=0;
        Connection connection=DB.getInstance().getConnection();
            try(PreparedStatement stmtProvera = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmt = connection.prepareStatement(upitStatus,PreparedStatement.RETURN_GENERATED_KEYS);    )
            {
                stmtProvera.setInt(1, packageId);
                boolean ishod=false;
                
                ResultSet rs = stmtProvera.executeQuery();
                if(rs.next())
                {
                    stmt.setInt(1, packageId);
                    ResultSet rsBroj = stmt.executeQuery();
                    if(rsBroj.next())
                    {
                       datum = rsBroj.getDate("Vreme");
                    }
                    else
                    {
                        rs.close();
                        rsBroj.close();
                        return null;
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
         
            return datum;  
    }

    @Override
    public List<Integer> getAllPackagesWithSpecificType(int type) {
        List<Integer> lista = new ArrayList<>();
        Connection connection=DB.getInstance().getConnection();
        String upit = "SELECT IdPak FROM Paket WHERE TipPaketa=?";
                try(
               PreparedStatement stmt = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            stmt.setInt(1, type);
            ResultSet rs = stmt.executeQuery();
            
            while(rs.next())
            {
                lista.add(rs.getInt("IdPak"));
            }
            rs.close();
        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return lista; 
    }

    @Override
    public List<Integer> getAllPackages() {
        List<Integer> lista = new ArrayList<>();
        Connection connection=DB.getInstance().getConnection();
        String upit = "SELECT IdPak FROM Paket";
                try(
               PreparedStatement stmt = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            ResultSet rs = stmt.executeQuery();
            
            while(rs.next())
            {
                lista.add(rs.getInt("IdPak"));
            }
            rs.close();
        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return lista; 
    }

    @Override
    public List<Integer> getDrive(String courierUsername) {
        List<Integer> lista = new ArrayList<>();
        String upitProvera = "SELECT IdVoz FROM Voznja WHERE KorisnickoIme=? and Zavrseno=0";
        String upit = "SELECT ZahtevPrevoz.IdPak FROM Voznja,VoznjaZahtev,ZahtevPrevoz WHERE Voznja.KorisnickoIme=? and Voznja.Zavrseno=0 and "
                + "ZahtevPrevoz.Status!=3 and VoznjaZahtev.IdPak=ZahtevPrevoz.IdPak and VoznjaZahtev.IdVoz=Voznja.IdVoz ORDER BY ZahtevPrevoz.Vreme ASC";
        int tipPaketa=0;
        Connection connection=DB.getInstance().getConnection();
            try(PreparedStatement stmtProvera = connection.prepareStatement(upitProvera,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmt = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);    )
            {
                stmtProvera.setString(1, courierUsername);
                boolean ishod=false;
                
                ResultSet rs = stmtProvera.executeQuery();
                if(rs.next())
                {
                    stmt.setString(1,courierUsername);
                    ResultSet rsUpit = stmt.executeQuery();

                    while(rsUpit.next())
                    {
                        lista.add(rsUpit.getInt("IdPak"));
                    }
                    rsUpit.close();
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
         
            return lista; 
    }
    
    public List<Integer> napraviVoznju(String courierUserName){
        List<Integer> lista = new ArrayList<>();
        String upit = "SELECT ZahtevPrevoz.IdPak FROM ZahtevPrevoz WHERE ZahtevPrevoz.KorisnickoIme=? and "
                + "ZahtevPrevoz.IdPak NOT IN (SELECT IdPak from VoznjaZahtev) and ZahtevPrevoz.Vreme IS NOT NULL ORDER BY ZahtevPrevoz.Vreme ASC";
        int tipPaketa=0;
        Connection connection=DB.getInstance().getConnection();
        try(PreparedStatement stmt = connection.prepareStatement(upit,PreparedStatement.RETURN_GENERATED_KEYS);)
            {
                stmt.setString(1, courierUserName);
                ResultSet rsUpit = stmt.executeQuery();
                while(rsUpit.next())
                {
                    lista.add(rsUpit.getInt("IdPak"));
                }
                rsUpit.close();  
                    
            } catch (SQLException ex) {
                Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        if (lista.size()>0)
        {
            int IdVoz=-1;
            String updateKurirStatus = "UPDATE Kurir SET Status=1 WHERE KorisnickoIme=?";
            String upitVoznjaInsert = "INSERT INTO Voznja (KorisnickoIme,Zavrseno,Profit) VALUES(?,0,0.000)";
            try(PreparedStatement stmt = connection.prepareStatement(upitVoznjaInsert,PreparedStatement.RETURN_GENERATED_KEYS);
                    PreparedStatement stmtKurir = connection.prepareStatement(updateKurirStatus,PreparedStatement.RETURN_GENERATED_KEYS);)
                {
                    stmtKurir.setString(1,courierUserName);
                    stmtKurir.executeUpdate();
                    
                    stmt.setString(1, courierUserName);
                    int broj_redova = stmt.executeUpdate();
                    if(broj_redova>0)
                    {
                        ResultSet rs = stmt.getGeneratedKeys();
                        rs.next();
                        IdVoz = rs.getInt(1);
                        rs.close();
                    }
                    
                    
                } catch (SQLException ex) {
                    Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
                }
            
            if(IdVoz!=-1)
            {
                for (int i = 0; i < lista.size(); i++) {
                    Integer IdPak = lista.get(i);
                    
                String upitVoznjaZahtevInsert = "INSERT INTO VoznjaZahtev (IdVoz,IdPak) VALUES(?,?)";
                try(PreparedStatement stmt = connection.prepareStatement(upitVoznjaZahtevInsert,PreparedStatement.RETURN_GENERATED_KEYS);)
                    {
                        stmt.setInt(1, IdVoz);
                        stmt.setInt(2, IdPak);
                        int broj_redova = stmt.executeUpdate();
                    } catch (SQLException ex) {
                        Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
                    }                    
                    
                }
            }
            
        
            
        }
        
        return lista;
        
    }
    
    
    public BigDecimal racunajDistancu(int opstinaPrva,int OpstinaDruga)
    {
        int XkoordPrva=0;
        int YkoordPrva=0;
        int XkoordDruga=0;
        int YkoordDruga=0;
        String upitPrvaOpstina = "SELECT Xkoord,Ykoord FROM Opstina WHERE IdOps=?";
        String upitDrugaOpstina = "SELECT Xkoord,Ykoord FROM Opstina WHERE IdOps=?";
        Connection connection=DB.getInstance().getConnection();
        try(PreparedStatement stmtPrva = connection.prepareStatement(upitPrvaOpstina,PreparedStatement.RETURN_GENERATED_KEYS);
            PreparedStatement stmtDruga = connection.prepareStatement(upitDrugaOpstina,PreparedStatement.RETURN_GENERATED_KEYS);)
            {
                stmtPrva.setInt(1, opstinaPrva);
                stmtDruga.setInt(1, OpstinaDruga);
                
                
                ResultSet rsPrva = stmtPrva.executeQuery();
                rsPrva.next();
                XkoordPrva=rsPrva.getInt("Xkoord");
                YkoordPrva=rsPrva.getInt("Ykoord");
                rsPrva.close();
                
                
                ResultSet rsDruga = stmtDruga.executeQuery();
                rsDruga.next();
                XkoordDruga=rsDruga.getInt("Xkoord");
                YkoordDruga=rsDruga.getInt("Ykoord");
                rsDruga.close();                
                    
            } catch (SQLException ex) {
                Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            }  
        
            BigDecimal distanca=null;
            
            int deltaX = XkoordPrva - XkoordDruga;
            int deltaY = YkoordPrva - YkoordDruga;
            

            BigDecimal deltaXKvadrat = BigDecimal.valueOf(deltaX).pow(2);
            BigDecimal deltaYKvadrat  = BigDecimal.valueOf(deltaY).pow(2);
            BigDecimal sumaKvadrata = deltaXKvadrat.add(deltaYKvadrat);
            distanca = new BigDecimal(Math.sqrt(sumaKvadrata.doubleValue()));

            distanca =  distanca.setScale(3, RoundingMode.HALF_UP);
            
            return distanca;
        
    }
    

    @Override
    public int driveNextPackage(String courierUserName) {
        Connection connection=DB.getInstance().getConnection();     
        String proveraKurir= "SELECT KorisnickoIme FROM Kurir WHERE KorisnickoIme=?";   
                try(
                PreparedStatement proverastmtKurir= connection.prepareStatement(proveraKurir,PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            
            proverastmtKurir.setString(1, courierUserName);
            
            ResultSet proversRS1 = proverastmtKurir.executeQuery();
            
            if(!proversRS1.next())       
            {
                proversRS1.close();
                return -2;
            }
            proversRS1.close();
        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        List<Integer> lista_paketa = getDrive(courierUserName);
        if(lista_paketa==null)
        {
            lista_paketa = napraviVoznju(courierUserName);
            if(lista_paketa.size()==0)
            {
                return -1;
            }
            
            int paket = lista_paketa.get(0);
            
            //Update BrojPoslatihPaketa Korisnika
            
            String updatePaketDrugi = "UPDATE ZahtevPrevoz SET Status=2 WHERE IdPak=?";
            String updateKorisnikBrPoslatih = "UPDATE Korisnik SET BrPoslatihPaketa=BrPoslatihPaketa+1 WHERE KorisnickoIme IN (SELECT KorisnickoIme FROM Paket WHERE Paket.IdPak=?)";
                    try(
                    PreparedStatement stmtPaket= connection.prepareStatement(updatePaketDrugi,PreparedStatement.RETURN_GENERATED_KEYS);
                    PreparedStatement stmtKorisnik= connection.prepareStatement(updateKorisnikBrPoslatih,PreparedStatement.RETURN_GENERATED_KEYS);)
            {

                stmtPaket.setInt(1, paket);
                stmtKorisnik.setInt(1, paket);
                
                stmtPaket.executeUpdate();
                stmtKorisnik.executeUpdate();
            } 
            catch (SQLException ex) {
                Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            }            
            
            
        }
        
        
        //Voznja jednog paketa
        
        int IdPak = lista_paketa.get(0);
        BigDecimal potrosnja=null;
        BigDecimal zarada=null;
        int tipVozila=-1;
        int opstinaPrva=0;
        int opstinaDruga=0;
        
        String upitOpstitne = "SELECT OpstinaPreuzimanje,OpstinaDostava,Cena FROM Paket,ZahtevPrevoz WHERE Paket.IdPak=? and ZahtevPrevoz.IdPak=Paket.IdPak";
        String upitPotrosnjaTipVozila= "SELECT Potrosnja,TipVozila FROM Vozilo,Kurir WHERE Kurir.KorisnickoIme=? and Kurir.RegistracioniBroj=Vozilo.RegistracioniBroj";   
                try(
                PreparedStatement stmtOpstine= connection.prepareStatement(upitOpstitne,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmtPotrosnjaTipVozila= connection.prepareStatement(upitPotrosnjaTipVozila,PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            
            stmtPotrosnjaTipVozila.setString(1, courierUserName);
            
            ResultSet proversRS1 = stmtPotrosnjaTipVozila.executeQuery();
            
            proversRS1.next();
            potrosnja = proversRS1.getBigDecimal("Potrosnja");
            tipVozila = proversRS1.getInt("TipVozila");

            proversRS1.close();
            
            stmtOpstine.setInt(1,IdPak);
            
            ResultSet RSOpstine = stmtOpstine.executeQuery();
            
            RSOpstine.next();
            opstinaPrva = RSOpstine.getInt("OpstinaPreuzimanje");
            opstinaDruga = RSOpstine.getInt("OpstinaDostava");
            zarada=RSOpstine.getBigDecimal("Cena");
            RSOpstine.close();            
        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }    
                
        BigDecimal razdaljina = racunajDistancu(opstinaPrva, opstinaDruga);
        
        
        
        BigDecimal oduzimac = razdaljina.multiply(potrosnja);
        oduzimac = oduzimac.multiply(CENA_GORIVA[tipVozila]);
        zarada = zarada.subtract(oduzimac);
        
        String updatePaket = "UPDATE ZahtevPrevoz SET Status=3 WHERE IdPak=?";
        String updateKurir = "UPDATE Kurir SET BrojIsporucenihPaketa=BrojIsporucenihPaketa+1 WHERE KorisnickoIme=?";
        String updateVoznja= "UPDATE Voznja SET Profit=Profit+? WHERE KorisnickoIme=? AND Zavrseno=0";  
                try(
                PreparedStatement stmtPaket= connection.prepareStatement(updatePaket,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmtKurir= connection.prepareStatement(updateKurir,PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement stmtVoznja= connection.prepareStatement(updateVoznja,PreparedStatement.RETURN_GENERATED_KEYS);)
        {
            
            stmtPaket.setInt(1, IdPak);
            stmtKurir.setString(1, courierUserName);
            
            stmtVoznja.setBigDecimal(1, zarada);
            stmtVoznja.setString(2, courierUserName);
            
            stmtPaket.executeUpdate();
            stmtKurir.executeUpdate();
            stmtVoznja.executeUpdate();
        } 
        catch (SQLException ex) {
            Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        } 
                
        if(lista_paketa.size()==1)
        {
            
            BigDecimal zaradaKurir = null;
            String upitZaradaKurir = "SELECT Profit FROM Voznja WHERE KorisnickoIme=? and Zavrseno=0"; 
                    try(
                    PreparedStatement stmtZaradaKurir= connection.prepareStatement(upitZaradaKurir,PreparedStatement.RETURN_GENERATED_KEYS);)
            {
                stmtZaradaKurir.setString(1,courierUserName);

                ResultSet RS= stmtZaradaKurir.executeQuery();

                RS.next();
                zaradaKurir = RS.getBigDecimal("Profit");
                RS.close();            
            } 
            catch (SQLException ex) {
                Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            }            
                    
            String updateVoznjaZavrseno = "UPDATE Voznja SET Zavrseno=1 WHERE Zavrseno=0 AND KorisnickoIme=?";
            String updateZaradaKurir= "UPDATE Kurir SET Status=0,Profit=Profit+? WHERE KorisnickoIme=?";  
                    try(
                    PreparedStatement stmtVoznjaZavrseno= connection.prepareStatement(updateVoznjaZavrseno,PreparedStatement.RETURN_GENERATED_KEYS);
                    PreparedStatement stmtZaradaKurir= connection.prepareStatement(updateZaradaKurir,PreparedStatement.RETURN_GENERATED_KEYS);)
            {

                stmtVoznjaZavrseno.setString(1, courierUserName);
                stmtZaradaKurir.setBigDecimal(1, zaradaKurir);
                stmtZaradaKurir.setString(2, courierUserName);
                stmtVoznjaZavrseno.executeUpdate();
                stmtZaradaKurir.executeUpdate();
            } 
            catch (SQLException ex) {
                Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            }                    
            
        }
        else
        {
            int IdPakSledeci = lista_paketa.get(1);
            
            opstinaPrva=opstinaDruga;
            String upitOpstitneDruga = "SELECT OpstinaPreuzimanje FROM Paket WHERE Paket.IdPak=?"; 
                    try(
                    PreparedStatement stmtOpstine= connection.prepareStatement(upitOpstitneDruga,PreparedStatement.RETURN_GENERATED_KEYS);)
            {
                stmtOpstine.setInt(1,IdPakSledeci);

                ResultSet RSOpstine = stmtOpstine.executeQuery();

                RSOpstine.next();
                opstinaDruga = RSOpstine.getInt("OpstinaPreuzimanje");
                RSOpstine.close();            
            } 
            catch (SQLException ex) {
                Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            } 
                    
            BigDecimal novaRazdaljina = racunajDistancu(opstinaPrva, opstinaDruga);
            BigDecimal smanjenje = novaRazdaljina.multiply(potrosnja);
            smanjenje = smanjenje.multiply(CENA_GORIVA[tipVozila]);

            String updatePaketDrugi = "UPDATE ZahtevPrevoz SET Status=2 WHERE IdPak=?";
            String updateKorisnikBrPoslatih = "UPDATE Korisnik SET BrPoslatihPaketa=BrPoslatihPaketa+1 WHERE KorisnickoIme IN (SELECT KorisnickoIme FROM Paket WHERE Paket.IdPak=?)";
            String updateVoznjaOduzimanje= "UPDATE Voznja SET Profit=Profit-? WHERE KorisnickoIme=? AND Zavrseno=0";  
                    try(
                    PreparedStatement stmtPaket= connection.prepareStatement(updatePaketDrugi,PreparedStatement.RETURN_GENERATED_KEYS);
                    PreparedStatement stmtKorisnik= connection.prepareStatement(updateKorisnikBrPoslatih,PreparedStatement.RETURN_GENERATED_KEYS);        
                    PreparedStatement stmtVoznja= connection.prepareStatement(updateVoznjaOduzimanje,PreparedStatement.RETURN_GENERATED_KEYS);)
            {

                stmtPaket.setInt(1, IdPakSledeci);
                stmtKorisnik.setInt(1, IdPakSledeci);

                stmtVoznja.setBigDecimal(1, smanjenje);
                stmtVoznja.setString(2, courierUserName);

                stmtPaket.executeUpdate();
                stmtVoznja.executeUpdate();
                stmtKorisnik.executeUpdate();
            } 
            catch (SQLException ex) {
                Logger.getLogger(mp210336_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            }             
        }

        return IdPak;
    }
    
    
    
    
    
}
