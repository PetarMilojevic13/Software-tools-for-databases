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
