IF OBJECT_ID('ContentDocNumber', 'U') IS NOT NULL
	DROP TABLE ContentDocNumber;
  
CREATE TABLE ContentDocNumber (
	pk INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
	ContentItemRecord_id INT NOT NULL,
	DocNumber VARCHAR(50) NOT NULL,
	INDEX idx_DocNumber NONCLUSTERED (DocNumber),
	INDEX idx_ContentItemRecord_id (ContentItemRecord_id)
);
GO

INSERT INTO ContentDocNumber(ContentItemRecord_id, DocNumber)
SELECT
	civr.ContentItemRecord_id, 
	CAST(civr.Data AS XML).value('(/Data/LibraryPolicy/Docnumber)[1]','varchar(50)')
FROM Orchard_Framework_ContentItemRecord AS cir
INNER JOIN Orchard_Framework_ContentTypeRecord 
    AS ctr ON ctr.Id = cir.ContentType_id
INNER JOIN Orchard_Framework_ContentItemVersionRecord 
    AS civr ON civr.ContentItemRecord_id = cir.Id
WHERE civr.Published = 1
AND ctr.Id = 12 -- LibraryPolicy
;

SELECT * FROM ContentDocNumber;