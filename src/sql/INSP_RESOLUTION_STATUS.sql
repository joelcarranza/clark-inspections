CREATE TABLE [dbo].[INSP_RESOLUTION_STATUS](
	[code] [varchar](10) NULL,
	[name] [varchar](100) NULL,
	[display_order] [int] NULL
) ON [PRIMARY]
GO

INSERT INTO [INSP_RESOLUTION_STATUS] (code,name, display_order) VALUES ('R', 'Resolved', 1);
