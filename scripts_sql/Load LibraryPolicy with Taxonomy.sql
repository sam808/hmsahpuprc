DROP TABLE _d;
DROP TABLE _c;
GO

CREATE TABLE _d (
	pk INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
	id INT NOT NULL,
	data XML NOT NULL,
	INDEX idx NONCLUSTERED (id)
);
CREATE TABLE _c (
	pk INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
	id INT NOT NULL,
	data XML NOT NULL,
	INDEX idx NONCLUSTERED (id)
);
GO

INSERT INTO _d(id, data)
SELECT
	civr.ContentItemRecord_id, 
	civr.Data
FROM Orchard_Framework_ContentItemRecord AS cir
INNER JOIN Orchard_Framework_ContentTypeRecord 
    AS ctr ON ctr.Id = cir.ContentType_id
INNER JOIN Orchard_Framework_ContentItemVersionRecord 
    AS civr ON civr.ContentItemRecord_id = cir.Id
WHERE civr.Published = 1
AND ctr.Id = 12 -- LibraryPolicy
;

INSERT INTO _c(id, data)
SELECT
	civr.ContentItemRecord_id,
	tcivr.Data
FROM
	Orchard_Framework_ContentItemVersionRecord  civr
	INNER JOIN Orchard_Taxonomies_TermContentItem tci
		ON civr.ContentItemRecord_id = tci.TermsPartRecord_id
	INNER JOIN Orchard_Taxonomies_TermPartRecord tpr 
		ON tci.TermRecord_id = tpr.Id
	INNER JOIN Orchard_Framework_ContentItemVersionRecord tcivr
		ON tcivr.ContentItemRecord_id = tci.TermRecord_id
;