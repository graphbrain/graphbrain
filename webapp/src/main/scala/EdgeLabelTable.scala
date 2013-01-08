package com.graphbrain.webapp

object EdgeLabelTable {
 val table = Map("occupation" -> "has occupation", "family" -> "is member of family", "kingdom" -> "is member of kingdom", "order" -> "is member of order", "class" -> "is member of class", "division" -> "is member of division", "director" -> "has director", "phylum" -> "is member of phylum", "binomial authority" -> "was named by binomial authority", "genus" -> "is member of genus", "current member" -> "has current member", "as in" -> "as in", "author" -> "has author", "band member" -> "has band member", "guest" -> "has guest", "subsequent work" -> "has subsequent work", "series" -> "is episode in series", "previous work" -> "has previous work", "writer" -> "has writer", "is part of" -> "is part of", "country" -> "belongs to country", "type" -> "is a", "department" -> "is in department", "region" -> "is in region", "birth place" -> "was born in", "team" -> "was member of team", "artist" -> "has artist", "record label" -> "has record label", "producer" -> "has producer", "genre" -> "belongs to genre", "album" -> "belongs to album", "location" -> "is located in", "origin" -> "originates from", "outflow" -> "has outflow", "product" -> "has product", "parent company" -> "has parent company", "city" -> "has city", "engine" -> "has motor", "variant of" -> "is variant of", "layout" -> "has layout", "body style" -> "has body style", "automobile platform" -> "has automobile platform", "college" -> "went to college", "instrument" -> "has instrument", "language" -> "has language", "media type" -> "has media type", "publisher" -> "has publisher", "location city" -> "is located in city", "industry" -> "belongs to industry", "location country" -> "is located in country", "mouth position" -> "has mouth position", "starring" -> "stars", "distributor" -> "has distributor", "music composer" -> "has music composer", "state" -> "is in state", "district" -> "is in district", "hometown" -> "has hometown", "recorded in" -> "was recorded in", "cinematography" -> "has cinematography by", "editing" -> "has editing by", "editor" -> "has editor", "academic discipline" -> "belongs to academic discipline", "successor" -> "has successor", "president" -> "was president of", "religion" -> "has religion", "death place" -> "died in", "field" -> "works in", "programme format" -> "has programme format", "broadcast network" -> "has broadcast network", "time zone" -> "has time zone", "grades" -> "has grades", "notable commander" -> "has notable commander", "military branch" -> "is military branch of", "military unit" -> "is military unit of", "patron" -> "has patron", "garrison" -> "has garrison", "relation" -> "has relation", "party" -> "belongs to party", "spouse" -> "has spouse", "child" -> "has child", "parent" -> "has parent", "known for" -> "is known for", "nationality" -> "has nationality", "former team" -> "has former team", "crosses" -> "crosses", "right tributary" -> "has right tributary", "sister station" -> "has sister station", "owner" -> "has owner", "broadcast area" -> "has broadcast area", "alma mater" -> "has alma mater", "leader name" -> "has leader name", "prime minister" -> "was prime minister for", "owning company" -> "has owning company", "government type" -> "has government type", "route end" -> "has route end", "headquarter" -> "is headquarter of", "commander" -> "had commander", "trainer" -> "had trainer", "operator" -> "had operator", "ground" -> "has ground", "capital" -> "has capital", "nearest city" -> "has nearest city", "population place" -> "are population in", "largest city" -> "has largest city", "located in area" -> "is located in area", "mouth place" -> "has mouth place", "residence" -> "has residence in", "route junction" -> "is route junction of", "mouth mountain" -> "has mouth mountain", "musical artist" -> "has musical artist", "musical band" -> "has musical band", "architect" -> "has architect", "format" -> "has format", "builder" -> "has builder", "is part of military conflict" -> "is part of military conflict", "battle" -> "battled in", "place" -> "took place in", "used in war" -> "was used in war", "territory" -> "has territory", "picture" -> "has picture", "associated musical artist" -> "has associated musical artist", "associated band" -> "has associated band", "canton" -> "is part of canton", "arrondissement" -> "is part of arrondissement", "resting place" -> "has resting place", "subsidiary" -> "had subsidiary", "key person" -> "had key person", "company" -> "had company", "channel" -> "was broadcast on channel", "county" -> "was in county", "league" -> "played in league", "former band member" -> "had former band member", "route start" -> "has route start", "school" -> "has school", "rival" -> "has rival", "province" -> "is in province", "highschool" -> "went to highschool", "affiliation" -> "has affiliation", "narrator" -> "has narrator", "voice" -> "has voice", "manager" -> "had manager", "manager club" -> "was manager of club", "statistic label" -> "hide?", "region served" -> "served region", "target airport" -> "has target airport", "hub airport" -> "has hub airport", "developer" -> "has developer", "state of origin" -> "has state of origin", "draft team" -> "has draft team", "ceremonial county" -> "belongs to ceremonial county", "major island" -> "has major island", "predecessor" -> "has predecessor", "leader function" -> "has leader function", "computing media" -> "has computing media", "computing platform" -> "has computing platform", "source country" -> "has source in country", "award" -> "received award", "literary genre" -> "belongs to literary genre", "opening theme" -> "has opening theme", "picture format" -> "has picture format", "river mouth" -> "has mouth", "part" -> "is part of", "game engine" -> "has game engine", "debut team" -> "has debut team", "discoverer" -> "has discoverer", "person" -> "has person", "executive producer" -> "has executive producer", "ending theme" -> "has ending theme", "network" -> "has network", "creator" -> "has creator", "influenced by" -> "is influenced by", "influenced" -> "influenced", "notable work" -> "has notable work", "founded by" -> "is founded by", "cover artist" -> "has cover artist", "railway rolling stock" -> "has railway rolling stock", "computing input" -> "has computing input", "architectural style" -> "has architectural style", "based on" -> "is based on", "lyrics" -> "has lyrics by", "music by" -> "has music by", "composer" -> "has composer", "neighboring municipality" -> "has neighbouring municipality", "related mean of transportation" -> "is related to mean of transportation", "assembly" -> "is assembled in", "manufacturer" -> "has manufacturer", "leader" -> "has leader", "growing grape" -> "grows grape", "subregion" -> "has subregion", "is part of wine region" -> "is part of wine region", "chancellor" -> "has chancellor", "foundation place" -> "has foundation place", "gender" -> "has gender", "ethnicity" -> "has ethnicity", "main interest" -> "has main interest", "philosophical school" -> "belongs to philosophical school", "beatified place" -> "was beatified in", "major shrine" -> "has major shrine", "tenant" -> "has tenant", "season" -> "has season", "chairman" -> "has chairman", "clubs record goalscorer" -> "has clubs record goalscorer", "sales" -> "has sales in", "mountain range" -> "belongs to mountain range", "highest position" -> "has highest position", "highest mountain" -> "has highest mountain", "highest place" -> "has highest place", "citizenship" -> "has citizenship", "doctoral advisor" -> "has doctoral advisor", "venerated in" -> "is venerated in", "distributing company" -> "has distributing company", "distributing label" -> "has distributing label", "wine region" -> "is grown in wine region", "species" -> "belongs to species", "related" -> "is related to", "relative" -> "has relative", "presenter" -> "has presenter", "jurisdiction" -> "has jurisdiction", "serving railway line" -> "serves railway line", "operated by" -> "is operated by", "source place" -> "has source place", "source mountain" -> "has source mountain", "monarch" -> "has monarch", "language family" -> "belongs to language family", "spoken in" -> "is spoken in", "rector" -> "had rector", "flag bearer" -> "had flag bearer", "second leader" -> "gave second leader", "deputy" -> "has deputy", "person function" -> "has person function", "nerve" -> "has nerve", "illustrator" -> "has illustrator", "intercommunality" -> "has intercommunality", "command structure" -> "has command structure", "mission" -> "had mission", "political party in legislature" -> "has political party in legislature", "political party of leader" -> "has political party of leader", "education" -> "was educated at", "death cause" -> "had death cause", "local authority" -> "has local authority", "authority" -> "has authority", "training" -> "was trained at", "service" -> "provides service", "governor" -> "had governor", "left tributary" -> "has left tributary", "translator" -> "has translator", "home stadium" -> "has home stadium", "coach" -> "has coach", "has variant" -> "has variant", "parent mountain peak" -> "has parent mountain peak", "cooling system" -> "is cooling system for", "head alloy" -> "has head alloy", "oil system" -> "has oil system", "block alloy" -> "has block alloy", "associate" -> "has associate", "inflow" -> "has inflow", "ethnic group" -> "has ethnic group", "partner" -> "had partner", "operating system" -> "works with operating system", "license" -> "has license", "first leader" -> "gave first leader", "ideology" -> "has ideology", "chairperson" -> "has chairperson", "youth wing" -> "has youth wing", "vice president" -> "had vice president", "campus" -> "has campus in", "branch from" -> "branches from", "designer" -> "has designer", "programming language" -> "was programmed in language", "billed" -> "is billed from", "federal state" -> "belongs to federal state", "doctoral student" -> "had doctoral student", "significant building" -> "designed significant building", "daylight saving time zone" -> "belongs to daylight saving time zone", "music subgenre" -> "has music subgenre", "derivative" -> "is derived from", "stylistic origin" -> "has stylistic origin", "music fusion genre" -> "belongs to music fusion genre", "non fiction subject" -> "has non fiction subject", "county seat" -> "has county seat", "first appearance" -> "first appeared in", "area of search" -> "belongs to area of search", "notable student" -> "has notable student", "former coach" -> "has former coach", "choreographer" -> "has choreographer", "former broadcast network" -> "has former broadcast network", "appointer" -> "was appointed by", "dean" -> "has dean", "destination" -> "has destination", "governing body" -> "has governing body", "source position" -> "has source position", "owning organisation" -> "belongs to owning organisation", "principal" -> "has principal", "profession" -> "has profession", "second driver" -> "had second driver", "second team" -> "had second team", "pole driver team" -> "had pole driver team", "third team" -> "had third team", "pole driver" -> "had pole driver", "last race" -> "had last race", "fastest driver team" -> "had fastest driver team", "first driver" -> "had first driver", "third driver" -> "had third driver", "first race" -> "had first race", "fastest driver" -> "had fastest driver team", "first driver team" -> "had first driver team", "first win" -> "had first win", "design company" -> "was designed by", "movement" -> "has movement", "notable wine" -> "makes notable wine", "varietals" -> "has varietals", "sport" -> "plays sport", "homeport" -> "has homeport", "twin city" -> "has twin city", "leader party" -> "had leader party", "source region" -> "has source region", "significant project" -> "has significant project", "era" -> "belongs to era", "country origin" -> "originates from country", "launch site" -> "has launch site", "rocket function" -> "has rocket function", "pastor" -> "had pastor", "employer" -> "had employer", "next mission" -> "precedes mission", "previous mission" -> "follows mission", "currency" -> "has currency", "using country" -> "is used in", "lieutenancy area" -> "belongs to lieutenancy area", "council area" -> "belongs to council area", "type of electrification" -> "has type of electrification", "portrayer" -> "was portrayed by", "associated act" -> "has associated act", "frazioni" -> "has frazioni", "saint" -> "has saint", "source" -> "has source", "mouth country" -> "has mouth in country", "power type" -> "has power type", "provost" -> "had provost", "head" -> "had head", "colour" -> "has colour", "other party" -> "has other party", "notable idea" -> "has notable idea", "association of local government" -> "has association of local government", "municipality" -> "has municipality", "cpu" -> "has cpu", "start point" -> "has start point", "previous event" -> "follows event", "following event" -> "precedes event", "binomial" -> "has binomial", "prospect team" -> "had prospect team", "heir" -> "had heir", "ingredient" -> "uses ingredient", "canonized by" -> "was canonized by", "beatified by" -> "was beatified by", "domain" -> "belongs to domain", "military rank" -> "had military rank", "home arena" -> "has home arena", "resting place position" -> "has resting place position", "aircraft recon" -> "has aircraft recon", "aircraft transport" -> "has aircraft transport", "former choreographer" -> "has former choreographer", "has junction with" -> "has junction with", "university" -> "went to university", "aircraft helicopter" -> "has aircraft helicopter attack", "aircraft helicopter utility" -> "has aircraft helicopter utility", "aircraft trainer" -> "has aircraft trainer", "similar" -> "is similar to", "parent organisation" -> "belongs to parent organisation", "equipment" -> "uses equipment", "lieutenant" -> "has lieutenant", "march" -> "has march", "rural municipality" -> "runs through rural municipality", "metropolitan borough" -> "belongs to metropolitan borough", "show judge" -> "has show judge", "fuel" -> "uses fuel", "is part of anatomical structure" -> "is part of anatomical structure", "sister newspaper" -> "has sister newspaper", "sister college" -> "has sister college", "promotion" -> "is promotion offered by", "vice chancellor" -> "has vice chancellor", "photographer" -> "has photographer", "lowest state" -> "has lowest state", "highest state" -> "has highest state", "mouth region" -> "has mouth region", "vice principal" -> "has vice principal", "denomination" -> "belongs to denomination", "secretary general" -> "has secretary general", "aircraft fighter" -> "has aircraft fighter", "aircraft helicopter attack" -> "has aircraft helicopter attack", "aircraft attack" -> "has aircraft attach", "second commander" -> "has second commander", "aircraft helicopter transport" -> "has aircraft helicopter transport", "wha draft team" -> "belonged to wha draft team", "committee in legislature" -> "has committee in legislature", "lowest place" -> "has lowest place", "religious head" -> "has religious head", "administrative collectivity" -> "has administrative collectivity", "borough" -> "belongs to borough", "last appearance" -> "last appeared in", "chief editor" -> "has chief editor", "associated rocket" -> "has associated rocket", "premiere place" -> "premiered at", "end point" -> "has end point", "former partner" -> "has former partner", "archipelago" -> "belongs to archipelago", "first ascent person" -> "has first ascent person", "lowest region" -> "has lowest region", "highest region" -> "has highest region", "iso code region" -> "has iso code region", "legal form" -> "has legal form", "film" -> "has film", "mayor" -> "has mayor", "previous editor" -> "has previous editor", "member of parliament" -> "has member of parliament", "body discovered" -> "has body discovered in", "source confluence" -> "has source confluence", "merged into party" -> "merged into party", "split from party" -> "split from party", "european parliament group" -> "belongs to parliament group", "national affiliation" -> "has national affiliation", "european affiliation" -> "has european affiliation", "source confluence mountain" -> "has source confluence mountain", "source confluence region" -> "has source confluence region", "source confluence position" -> "has source confluence position", "source confluence place" -> "has source confluence place", "border" -> "borders on", "school patron" -> "has school patron", "maintained by" -> "is maintained by", "government position" -> "has government position", "river" -> "has river", "athletics" -> "has athletics", "compiler" -> "was compiled by", "animal" -> "has animal", "government" -> "is governed by", "port" -> "has port", "target space station" -> "has target space station", "rocket" -> "has rocket", "crew" -> "has crew", "religious head label" -> "hide?", "second driver country" -> "had second driver country", "international affiliation" -> "has international affiliation", "current partner" -> "has current partner", "plant" -> "has plant", "state delegate" -> "was state delegate of", "available smart card" -> "has available smart card", "drains to" -> "drains to", "drains from" -> "drains from", "vein" -> "has vein", "branch to" -> "branches to", "artery" -> "has artery", "school board" -> "has school board", "railway line using tunnel" -> "is tunnel for railway line", "chain" -> "belongs to chain", "crew member" -> "has crew member", "launch pad" -> "has launch pad", "largest settlement" -> "has largest settlement", "academic advisor" -> "has academic advisor", "place of burial" -> "is buried at", "alumni" -> "has alumni", "booster" -> "has booster", "highway system" -> "has highway system", "game artist" -> "has game artist", "lowest position" -> "has lowest position", "spokesperson" -> "has spokesperson", "ceo" -> "has ceo", "gene location" -> "has gene location", "official language" -> "has official language", "previous infrastructure" -> "has previous infrastructure", "vehicle" -> "has vehicle", "sport country" -> "has sport country", "opponent" -> "has opponent", "unitary authority" -> "has unitary authority", "island" -> "has island", "biome" -> "belongs to biome", "administrative district" -> "belongs to administrative district", "original language" -> "has original language", "capital position" -> "has capital position", "supplies" -> "supplies", "board" -> "belongs to board", "house" -> "has house", "sport governing body" -> "has sport governing body", "retired rocket" -> "has retired rocket", "comparable" -> "is comparable with", "maiden flight rocket" -> "has maiden flight rocket", "aircraft patrol" -> "has aircraft patrol", "running mate" -> "had running mate", "nominee" -> "was nominee in", "incumbent" -> "had incumbent", "government region" -> "has government region", "lowest mountain" -> "has lowest mountain", "founding person" -> "was founded by", "precursor" -> "has precursor", "curator" -> "has curator", "creative director" -> "has creative director", "story editor" -> "has story editor", "child organisation" -> "has child organisation", "engine type" -> "has engine type", "significant design" -> "has significant design", "governor general" -> "had governor general", "assistant principal" -> "has assistant principal", "construction material" -> "has construction material", "joint community" -> "has joint community", "closing film" -> "had closing film", "opening film" -> "had opening film", "alliance" -> "has alliance", "nobel laureates" -> "has nobel laureates", "fuel system" -> "has fuel system", "principal area" -> "has principal area", "recent winner" -> "has recent winner", "route end location" -> "has route end location", "anthem" -> "has anthem", "wins at p g a" -> "wins at p g a", "third driver country" -> "had third driver from country", "pole driver country" -> "had pole driver from country", "first driver country" -> "had first driver from country", "twin country" -> "has twin country", "fourth commander" -> "has fourth commander", "aircraft helicopter cargo" -> "has aircraft helicopter cargo", "project participant" -> "has project participant", "funded by" -> "is funded by", "project coordinator" -> "has project coordinator", "orogeny" -> "has orogeny", "trustee" -> "has trustee", "superintendent" -> "has superintendent", "custodian" -> "has custodian", "number of classrooms" -> "has classrooms", "language regulator" -> "has language regulator", "landing vehicle" -> "has landing vehicle", "launch vehicle" -> "has launch vehicle", "nrhp type" -> "has nrhp type", "innervates" -> "innervates", "rebuilder" -> "was rebuilt by", "canonized place" -> "was canonized in", "creator of dish" -> "was created by", "principal engineer" -> "has principal engineer", "honours" -> "has honours", "structural system" -> "has structural system", "event" -> "participated in event", "last win" -> "had last win", "aircraft helicopter multirole" -> "has aircraft helicopter multirole", "webcast" -> "has webcast", "royal anthem" -> "had royal anthem", "congressional district" -> "belongs to congressional district", "aircraft electronic" -> "has aircraft electronic", "aircraft bomber" -> "has aircraft bomber", "lymph" -> "has lymph", "first winner" -> "has first winner", "most wins" -> "has most wins by", "wine produced" -> "produces wine", "past member" -> "has past member", "regional language" -> "has regional language", "spacecraft" -> "used spacecraft", "resolution" -> "has resolution", "management" -> "has management", "teaching staff" -> "has teaching staff", "lounge" -> "has lounge", "chef" -> "has chef", "connotation" -> "has connotation", "former highschool" -> "has former highschool", "route start location" -> "has route start location", "third commander" -> "has third commander", "officer in charge" -> "has officer in charge", "main organ" -> "has main organ", "brand" -> "has brand", "vice prime minister" -> "had vice prime minister", "wins at n w i d e" -> "wins at n w i d e", "fastest driver country" -> "had fastest driver from country", "engineer" -> "has engineer", "boiler pressure" -> "has boiler pressure", "river branch of" -> "is river branch of", "river branch" -> "has river branch", "category" -> "belongs to category", "vice principal label" -> "hide?", "sheading" -> "has sheading", "parish" -> "has parish", "managing editor" -> "has managing editor", "associate editor" -> "has associate editor", "chaplain" -> "has chaplain", "management mountain" -> "has management mountain", "management place" -> "has management place", "management position" -> "has management position", "aircraft interceptor" -> "has aircraft interceptor", "meeting road" -> "meets road", "summer appearances" -> "makes summer appearances as", "winter appearances" -> "makes winter appearances as", "building" -> "has building", "first popular vote" -> "has first popular vote in", "second popular vote" -> "has second popular vote in", "current production" -> "has current production", "map" -> "is found on map", "wins at aus" -> "wins at aus", "wins at japan" -> "wins at japan", "wins at other tournaments" -> "wins at other tournements", "head chef" -> "has head chef", "orthologous gene" -> "has orthologous gene", "country with first astronaut" -> "has first astronaut from country", "iso code" -> "have iso code", "source confluence state" -> "has source confluence in state", "beltway city" -> "has beltway city", "component" -> "has component", "selection" -> "belongs to selection", "government place" -> "has government place", "government mountain" -> "has government mountain", "voice type" -> "has voice type", "last launch rocket" -> "has last launch rocket", "waterway through tunnel" -> "is waterway tunnel through", "country with first spaceflight" -> "has first spaceflight from country", "country with first satellite launched" -> "has first satellite launched from country", "country with first satellite" -> "has first satellite from country", "act score" -> "has act score", "first flight" -> "has first flight", "spur of" -> "is spur of", "address in road" -> "is address in road", "subsequent infrastructure" -> "has subsequent infrastructure", "coached team" -> "coached team", "left child" -> "has left child", "named after" -> "is named after", "wins at challenges" -> "wins at challenges", "organ system" -> "has organ system", "chair label" -> "hide?", "aircraft helicopter observation" -> "has aircraft helicopter observation", "last flight" -> "has last flight", "original end point" -> "has original end point", "wins at majors" -> "wins at majors", "general manager" -> "has general manager", "capital place" -> "has capital place", "capital mountain" -> "has capital mountain", "education system" -> "has education system", "original start point" -> "has original start point", "first launch rocket" -> "has first launch rocket", "national olympic committee" -> "has national olympic committee", "boiler" -> "has boiler", "sat score" -> "has sat score", "government country" -> "has government country", "wins in europe" -> "wins in europe", "right child" -> "has right child", "capital country" -> "has capital country", "capital region" -> "has capital region", "headteacher" -> "has headteacher")
}

