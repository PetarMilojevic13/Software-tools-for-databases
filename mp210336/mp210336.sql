CREATE DATABASE PROJEKAT
go

USE PROJEKAT
go

CREATE TABLE [Administrator]
( 
	[KorisnickoIme]      varchar(100)  NOT NULL 
)
go

CREATE TABLE [Grad]
( 
	[Naziv]              varchar(100)  NULL ,
	[PostanskiBroj]      varchar(100)  NULL ,
	[IdGra]              integer  IDENTITY ( 1,1 )  NOT NULL 
)
go

CREATE TABLE [Korisnik]
( 
	[Ime]                varchar(100)  NULL ,
	[Prezime]            varchar(100)  NULL ,
	[KorisnickoIme]      varchar(100)  NOT NULL ,
	[Sifra]              varchar(100)  NULL ,
	[BrPoslatihPaketa]   integer  NULL 
	CONSTRAINT [Nula_1744847140]
		 DEFAULT  0
)
go

CREATE TABLE [Kurir]
( 
	[KorisnickoIme]      varchar(100)  NOT NULL ,
	[BrojIsporucenihPaketa] integer  NULL 
	CONSTRAINT [Nula_252695035]
		 DEFAULT  0,
	[Profit]             decimal(10,3)  NULL 
	CONSTRAINT [NulaDecimal_601448555]
		 DEFAULT  0.000,
	[Status]             integer  NULL 
	CONSTRAINT [StatusKurira_109272485]
		CHECK  ( [Status]=0 OR [Status]=1 ),
	[RegistracioniBroj]  varchar(100)  NOT NULL 
)
go

CREATE TABLE [Opstina]
( 
	[IdOps]              integer  IDENTITY ( 1,1 )  NOT NULL ,
	[Naziv]              varchar(100)  NULL ,
	[Xkoord]             integer  NULL ,
	[Ykoord]             integer  NULL ,
	[IdGra]              integer  NOT NULL 
)
go

CREATE TABLE [Paket]
( 
	[IdPak]              integer  IDENTITY ( 1,1 )  NOT NULL ,
	[OpstinaPreuzimanje] integer  NOT NULL ,
	[OpstinaDostava]     integer  NOT NULL ,
	[TipPaketa]          integer  NULL 
	CONSTRAINT [TipPaketa_274677079]
		CHECK  ( [TipPaketa]=0 OR [TipPaketa]=1 OR [TipPaketa]=2 ),
	[TezinaPaketa]       decimal(10,3)  NULL ,
	[KorisnickoIme]      varchar(100)  NOT NULL 
)
go

CREATE TABLE [Ponuda]
( 
	[IdPak]              integer  NOT NULL ,
	[KorisnickoIme]      varchar(100)  NOT NULL ,
	[Procenat]           decimal(10,3)  NULL ,
	[IdPon]              integer  IDENTITY ( 1,1 )  NOT NULL 
)
go

CREATE TABLE [Vozilo]
( 
	[RegistracioniBroj]  varchar(100)  NOT NULL ,
	[TipVozila]          integer  NULL 
	CONSTRAINT [TipGorivaVozilo_673432743]
		CHECK  ( [TipVozila]=0 OR [TipVozila]=1 OR [TipVozila]=2 ),
	[Potrosnja]          decimal(10,3)  NULL 
)
go

CREATE TABLE [Voznja]
( 
	[IdVoz]              integer  IDENTITY ( 1,1 )  NOT NULL ,
	[Zavrseno]           integer  NULL ,
	[KorisnickoIme]      varchar(100)  NOT NULL ,
	[Profit]             decimal(10,3)  NULL 
)
go

CREATE TABLE [VoznjaZahtev]
( 
	[IdVoz]              integer  NOT NULL ,
	[IdPak]              integer  NOT NULL 
)
go

CREATE TABLE [ZahtevKurir]
( 
	[RegistracioniBroj]  varchar(100)  NOT NULL ,
	[KorisnickoIme]      varchar(100)  NOT NULL 
)
go

CREATE TABLE [ZahtevPrevoz]
( 
	[Status]             integer  NULL 
	CONSTRAINT [StatusZahteva_839877148]
		CHECK  ( [Status]=0 OR [Status]=1 OR [Status]=2 OR [Status]=3 ),
	[Cena]               decimal(10,3)  NULL ,
	[Vreme]              datetime  NULL ,
	[IdPak]              integer  NOT NULL ,
	[Procenat]           decimal(10,3)  NULL ,
	[KorisnickoIme]      varchar(100)  NULL 
)
go

ALTER TABLE [Administrator]
	ADD CONSTRAINT [XPKAdministrator] PRIMARY KEY  CLUSTERED ([KorisnickoIme] ASC)
go

ALTER TABLE [Grad]
	ADD CONSTRAINT [XPKGrad] PRIMARY KEY  CLUSTERED ([IdGra] ASC)
go

ALTER TABLE [Grad]
	ADD CONSTRAINT [XAK1Grad] UNIQUE ([Naziv]  ASC)
go

ALTER TABLE [Korisnik]
	ADD CONSTRAINT [XPKKorisnik] PRIMARY KEY  CLUSTERED ([KorisnickoIme] ASC)
go

ALTER TABLE [Kurir]
	ADD CONSTRAINT [XPKKurir] PRIMARY KEY  CLUSTERED ([KorisnickoIme] ASC)
go

ALTER TABLE [Opstina]
	ADD CONSTRAINT [XPKOpstina] PRIMARY KEY  CLUSTERED ([IdOps] ASC)
go

ALTER TABLE [Paket]
	ADD CONSTRAINT [XPKPaket] PRIMARY KEY  CLUSTERED ([IdPak] ASC)
go

ALTER TABLE [Ponuda]
	ADD CONSTRAINT [XPKPonuda] PRIMARY KEY  CLUSTERED ([IdPon] ASC)
go

ALTER TABLE [Vozilo]
	ADD CONSTRAINT [XPKVozilo] PRIMARY KEY  CLUSTERED ([RegistracioniBroj] ASC)
go

ALTER TABLE [Voznja]
	ADD CONSTRAINT [XPKVoznja] PRIMARY KEY  CLUSTERED ([IdVoz] ASC)
go

ALTER TABLE [VoznjaZahtev]
	ADD CONSTRAINT [XPKVoznjaZahtev] PRIMARY KEY  CLUSTERED ([IdVoz] ASC,[IdPak] ASC)
go

ALTER TABLE [ZahtevKurir]
	ADD CONSTRAINT [XPKZahtevKurir] PRIMARY KEY  CLUSTERED ([KorisnickoIme] ASC)
go

ALTER TABLE [ZahtevPrevoz]
	ADD CONSTRAINT [XPKZahtevPrevoz] PRIMARY KEY  CLUSTERED ([IdPak] ASC)
go


ALTER TABLE [Administrator]
	ADD CONSTRAINT [R_3] FOREIGN KEY ([KorisnickoIme]) REFERENCES [Korisnik]([KorisnickoIme])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go


ALTER TABLE [Kurir]
	ADD CONSTRAINT [R_5] FOREIGN KEY ([KorisnickoIme]) REFERENCES [Korisnik]([KorisnickoIme])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go

ALTER TABLE [Kurir]
	ADD CONSTRAINT [R_6] FOREIGN KEY ([RegistracioniBroj]) REFERENCES [Vozilo]([RegistracioniBroj])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go


ALTER TABLE [Opstina]
	ADD CONSTRAINT [R_1] FOREIGN KEY ([IdGra]) REFERENCES [Grad]([IdGra])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go


ALTER TABLE [Paket]
	ADD CONSTRAINT [R_8] FOREIGN KEY ([OpstinaPreuzimanje]) REFERENCES [Opstina]([IdOps])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Paket]
	ADD CONSTRAINT [R_9] FOREIGN KEY ([OpstinaDostava]) REFERENCES [Opstina]([IdOps])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Paket]
	ADD CONSTRAINT [R_21] FOREIGN KEY ([KorisnickoIme]) REFERENCES [Korisnik]([KorisnickoIme])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Ponuda]
	ADD CONSTRAINT [R_10] FOREIGN KEY ([IdPak]) REFERENCES [Paket]([IdPak])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go

ALTER TABLE [Ponuda]
	ADD CONSTRAINT [R_11] FOREIGN KEY ([KorisnickoIme]) REFERENCES [Kurir]([KorisnickoIme])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go


ALTER TABLE [Voznja]
	ADD CONSTRAINT [R_16] FOREIGN KEY ([KorisnickoIme]) REFERENCES [Kurir]([KorisnickoIme])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go


ALTER TABLE [VoznjaZahtev]
	ADD CONSTRAINT [R_17] FOREIGN KEY ([IdVoz]) REFERENCES [Voznja]([IdVoz])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go

ALTER TABLE [VoznjaZahtev]
	ADD CONSTRAINT [R_18] FOREIGN KEY ([IdPak]) REFERENCES [ZahtevPrevoz]([IdPak])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go


ALTER TABLE [ZahtevKurir]
	ADD CONSTRAINT [R_4] FOREIGN KEY ([RegistracioniBroj]) REFERENCES [Vozilo]([RegistracioniBroj])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go

ALTER TABLE [ZahtevKurir]
	ADD CONSTRAINT [R_23] FOREIGN KEY ([KorisnickoIme]) REFERENCES [Korisnik]([KorisnickoIme])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [ZahtevPrevoz]
	ADD CONSTRAINT [R_15] FOREIGN KEY ([IdPak]) REFERENCES [Paket]([IdPak])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go

ALTER TABLE [ZahtevPrevoz]
	ADD CONSTRAINT [R_24] FOREIGN KEY ([KorisnickoIme]) REFERENCES [Kurir]([KorisnickoIme])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go



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


USE [PROJEKAT]
GO
/****** Object:  Trigger [dbo].[TR_TransportOffer_biranjePonude]    Script Date: 7/5/2024 1:22:13 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE TRIGGER [dbo].[TR_TransportOffer_biranjePonude]
   ON  [dbo].[ZahtevPrevoz]
   AFTER UPDATE
AS 
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

	declare @IdPak int
	declare @Vreme Date

	declare @IdPakStari int
	declare @VremeStari Date

	declare @kursorStari cursor
	set @kursorStari = cursor for
	select IdPak,Vreme
	from deleted

    declare @kursor cursor
	set @kursor = cursor for
	select IdPak,Vreme
	from inserted

	open @kursorStari
	fetch next from @kursorStari
	into @IdPakStari,@VremeStari

	open @kursor
	fetch next from @kursor
	into @IdPak,@Vreme

	while @@FETCH_STATUS=0
	begin

		if (@Vreme is not null and @VremeStari is null)
		begin
			DELETE FROM Ponuda 
			WHERE IdPak=@IdPak
		end

		fetch next from @kursor
		into @IdPak,@Vreme

		fetch next from @kursorStari
		into @IdPakStari,@VremeStari
	end

	close @kursor
	deallocate @kursor

END
