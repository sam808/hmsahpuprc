SELECT
	-- _d.data,
	_d.data.value('(/Data/TitlePart/@Title)[1]','varchar(255)') AS Title,
	_d.data.value('(/Data/LibraryPolicy/Docnumber)[1]','varchar(255)') AS Docnumber,
	_d.data.value('(/Data/LibraryPolicy/PublishedBy)[1]','varchar(255)') AS PublishedBy,
	_d.data.value('(/Data/LibraryPolicy/ContentOwner)[1]','varchar(255)') AS CurrentOwner,
	-- _c.data,
	(SELECT STUFF((
		SELECT ',' + _c.data.value('(/Data/TitlePart/@Title)[1]','varchar(255)')
		 FROM _c WHERE _c.id = _d.id FOR XML PATH ('')
		),1,1,'')
	)
	FROM _d
;


SELECT
	-- _d.data,
	_d.data.value('(/Data/TitlePart/@Title)[1]','varchar(255)') AS Title,
	_d.data.value('(/Data/LibraryPolicy/Docnumber)[1]','varchar(255)') AS Docnumber,
	_d.data.value('(/Data/LibraryPolicy/PublishedBy)[1]','varchar(255)') AS PublishedBy,
	_d.data.value('(/Data/LibraryPolicy/ContentOwner)[1]','varchar(255)') AS CurrentOwner,
	-- _c.data,
	_c.data.value('(/Data/TitlePart/@Title)[1]','varchar(255)') AS Category
	FROM _d
	INNER JOIN _c ON _c.id = _d.id
;