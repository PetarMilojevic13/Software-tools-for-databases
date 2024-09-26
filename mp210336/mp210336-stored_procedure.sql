
CREATE PROCEDURE postaviKurira 

	@KorisnickoIme varchar(100) 
	
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    declare @BrojacProvera int
	set @BrojacProvera=0

	SELECT @BrojacProvera=count(*)
	from ZahtevKurir
	where  KorisnickoIme=@KorisnickoIme

	if(@BrojacProvera=0)
	begin
		print 'Zahtev za dato korisnicko ime ne postoji';
		return -5;
	end

	declare @Registracija varchar(100)

	SELECT @Registracija=RegistracioniBroj
	from ZahtevKurir
	where  KorisnickoIme=@KorisnickoIme



	SELECT @BrojacProvera=count(*)
	from Vozilo
	where RegistracioniBroj=@Registracija

	if(@BrojacProvera=0)
	begin
		DELETE FROM ZahtevKurir WHERE KorisnickoIme=@KorisnickoIme;
		print 'Vozilo sa datom registracijom ne postoji';
		return -1;
	end


	SELECT @BrojacProvera=count(*)
	from Korisnik
	where KorisnickoIme=@KorisnickoIme

	if(@BrojacProvera=0)
	begin
		DELETE FROM ZahtevKurir WHERE KorisnickoIme=@KorisnickoIme;
		print 'Korisnik sa datim korisnickim imenom ne postoji';
		return -2;
	end

	SELECT @BrojacProvera=count(*)
	from Kurir
	where KorisnickoIme=@KorisnickoIme

	if(@BrojacProvera>0)
	begin
		DELETE FROM ZahtevKurir WHERE KorisnickoIme=@KorisnickoIme;
		print 'Korisnik je vec kurir';
		return -3;
	end

	SELECT @BrojacProvera=count(*)
	from Kurir
	where RegistracioniBroj=@Registracija

	if(@BrojacProvera>0)
	begin
		DELETE FROM ZahtevKurir WHERE KorisnickoIme=@KorisnickoIme;
		print 'Vozilo je vec zauzeto od strane drugog kurira';
		return -4;
	end

	INSERT INTO Kurir (KorisnickoIme,BrojIsporucenihPaketa,RegistracioniBroj,Profit,Status) 
	VALUES(@KorisnickoIme,0,@Registracija,0.000,0);

	DELETE FROM ZahtevKurir WHERE KorisnickoIme=@KorisnickoIme;

	print 'Korisnik uspesno dodat kao kurir';
	return 0;

END
GO
