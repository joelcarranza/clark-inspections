CREATE TABLE [dbo].[INSP_ISSUE_PRIORITY](
	[code] [varchar](10) NULL,
	[name] [varchar](100) NULL,
	[display_order] [int] NULL
) ON [PRIMARY]
GO

INSERT INTO [INSP_ISSUE_PRIORITY] (code,name, display_order) VALUES ('L', 'Low', 1);
INSERT INTO [INSP_ISSUE_PRIORITY] (code,name, display_order) VALUES ('H', 'High', 2);
INSERT INTO [INSP_ISSUE_PRIORITY] (code,name, display_order) VALUES ('C', 'Critical', 2);
