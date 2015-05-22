package org.mtransit.parser.ca_calgary_transit_bus;

import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirectionType;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MSpec;
import org.mtransit.parser.mt.data.MTrip;

// https://www.calgarytransit.com/developer-resources
// https://data.calgary.ca/OpenData/Pages/DatasetDetails.aspx?DatasetID=PDC0-99999-99999-00501-P(CITYonlineDefault)
// https://data.calgary.ca/_layouts/OpenData/DownloadDataset.ashx?Format=FILE&DatasetId=PDC0-99999-99999-00501-P(CITYonlineDefault)&VariantId=5(CITYonlineDefault)
public class CalgaryTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-calgary-transit-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new CalgaryTransitBusAgencyTools().start(args);
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		System.out.printf("Generating Calgary Transit bus data...\n");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("Generating Calgary Transit bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public long getRouteId(GRoute gRoute) {
		return Long.parseLong(gRoute.route_short_name); // using route short name as route ID
	}

	private static final Pattern CLEAN_STREET_POINT = Pattern.compile("((\\s)*(ave|st|mt)\\.(\\s)*)", Pattern.CASE_INSENSITIVE);
	private static final String CLEAN_AVE_POINT_REPLACEMENT = "$2$3$4";

	@Override
	public String getRouteLongName(GRoute gRoute) {
		String gRouteLongName = gRoute.route_long_name;
		gRouteLongName = MSpec.CLEAN_SLASHES.matcher(gRouteLongName).replaceAll(MSpec.CLEAN_SLASHES_REPLACEMENT);
		gRouteLongName = CLEAN_STREET_POINT.matcher(gRouteLongName).replaceAll(CLEAN_AVE_POINT_REPLACEMENT);
		gRouteLongName = MSpec.cleanStreetTypes(gRouteLongName);
		return MSpec.cleanLabel(gRouteLongName);
	}

	private static final String AGENCY_COLOR_RED = "B83A3F"; // LIGHT RED (from web site CSS)

	private static final String AGENCY_COLOR = AGENCY_COLOR_RED;

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final String COLOR_BUS_ROUTES = "004B85"; // BLUE (from PDF map)
	private static final String COLOR_BUS_ROUTES_EXPRESS = "00BBE5"; // LIGHT BLUE (from PDF map)
	private static final String COLOR_BUS_ROUTES_BRT = "ED1C2E"; // RED (from PDF map)
	private static final String COLOR_BUS_ROUTES_SCHOOL = "E4A024"; // YELLOW (from PDF map)

	@Override
	public String getRouteColor(GRoute gRoute) {
		int rsn = Integer.parseInt(gRoute.route_short_name);
		switch (rsn) {
		// @formatter:off
		case 1: return null;
		case 2: return null;
		case 3: return null;
		case 4: return null;
		case 5: return null;
		case 6: return null;
		case 7: return null;
		case 8: return null;
		case 9: return null;
		case 10: return null;
		case 11: return null;
		case 12: return null;
		case 13: return null;
		case 14: return null;
		case 15: return null;
		case 16: return null;
		case 17: return null;
		case 18: return null;
		case 19: return null;
		case 20: return null;
		case 21: return null;
		case 22: return COLOR_BUS_ROUTES_EXPRESS;
		case 23: return COLOR_BUS_ROUTES_EXPRESS;
		case 24: return null;
		case 25: return null;
		case 26: return null;
		case 27: return COLOR_BUS_ROUTES;
		case 28: return null;
		case 29: return null;
		case 30: return COLOR_BUS_ROUTES;
		case 32: return null;
		case 33: return COLOR_BUS_ROUTES;
		case 34: return COLOR_BUS_ROUTES;
		case 35: return COLOR_BUS_ROUTES;
		case 36: return null;
		case 37: return null;
		case 38: return null;
		case 39: return COLOR_BUS_ROUTES;
		case 40: return null;
		case 41: return null;
		case 42: return null;
		case 43: return null;
		case 44: return COLOR_BUS_ROUTES;
		case 45: return null;
		case 46: return null;
		case 47: return COLOR_BUS_ROUTES;
		case 48: return null;
		case 49: return null;
		case 50: return null;
		case 51: return null;
		case 52: return null;
		case 54: return null;
		case 55: return null;
		case 56: return null;
		case 57: return null;
		case 60: return null;
		case 61: return COLOR_BUS_ROUTES;
		case 62: return COLOR_BUS_ROUTES_EXPRESS;
		case 63: return COLOR_BUS_ROUTES_EXPRESS;
		case 64: return COLOR_BUS_ROUTES_EXPRESS;
		case 66: return COLOR_BUS_ROUTES_EXPRESS;
		case 69: return COLOR_BUS_ROUTES;
		case 70: return COLOR_BUS_ROUTES_EXPRESS;
		case 71: return COLOR_BUS_ROUTES;
		case 72: return null;
		case 73: return null;
		case 74: return null;
		case 75: return COLOR_BUS_ROUTES_EXPRESS;
		case 76: return null;
		case 77: return COLOR_BUS_ROUTES;
		case 78: return null;
		case 79: return null;
		case 80: return null;
		case 81: return COLOR_BUS_ROUTES;
		case 83: return null;
		case 84: return COLOR_BUS_ROUTES;
		case 85: return null;
		case 86: return null;
		case 88: return null;
		case 89: return COLOR_BUS_ROUTES;
		case 91: return COLOR_BUS_ROUTES;
		case 92: return null;
		case 93: return COLOR_BUS_ROUTES;
		case 94: return COLOR_BUS_ROUTES;
		case 95: return COLOR_BUS_ROUTES;
		case 96: return null;
		case 98: return COLOR_BUS_ROUTES;
		case 100: return null;
		case 102: return COLOR_BUS_ROUTES_EXPRESS;
		case 103: return COLOR_BUS_ROUTES_EXPRESS;
		case 107: return COLOR_BUS_ROUTES;
		case 109: return COLOR_BUS_ROUTES_EXPRESS;
		case 110: return COLOR_BUS_ROUTES_EXPRESS;
		case 112: return null;
		case 114: return null;
		case 116: return COLOR_BUS_ROUTES_EXPRESS;
		case 117: return COLOR_BUS_ROUTES_EXPRESS;
		case 118: return null;
		case 122: return COLOR_BUS_ROUTES;
		case 125: return COLOR_BUS_ROUTES_EXPRESS;
		case 126: return COLOR_BUS_ROUTES_EXPRESS;
		case 127: return null;
		case 133: return COLOR_BUS_ROUTES_EXPRESS;
		case 136: return COLOR_BUS_ROUTES;
		case 137: return null;
		case 142: return COLOR_BUS_ROUTES_EXPRESS;
		case 143: return null;
		case 145: return COLOR_BUS_ROUTES;
		case 146: return COLOR_BUS_ROUTES;
		case 151: return COLOR_BUS_ROUTES_EXPRESS;
		case 152: return COLOR_BUS_ROUTES;
		case 153: return COLOR_BUS_ROUTES;
		case 154: return null;
		case 157: return null;
		case 158: return null;
		case 159: return COLOR_BUS_ROUTES;
		case 169: return null;
		case 174: return null;
		case 176: return COLOR_BUS_ROUTES_EXPRESS;
		case 178: return COLOR_BUS_ROUTES;
		case 181: return COLOR_BUS_ROUTES_EXPRESS;
		case 182: return COLOR_BUS_ROUTES_EXPRESS;
		case 199: return null;
		case 299: return null;
		case 300: return COLOR_BUS_ROUTES_BRT;
		case 301: return COLOR_BUS_ROUTES_BRT;
		case 302: return COLOR_BUS_ROUTES_BRT;
		case 305: return COLOR_BUS_ROUTES_BRT;
		case 306: return COLOR_BUS_ROUTES_BRT;
		case 402: return COLOR_BUS_ROUTES;
		case 404: return COLOR_BUS_ROUTES;
		case 405: return COLOR_BUS_ROUTES;
		case 406: return COLOR_BUS_ROUTES;
		case 407: return COLOR_BUS_ROUTES;
		case 408: return null;
		case 409: return COLOR_BUS_ROUTES;
		case 410: return COLOR_BUS_ROUTES;
		case 411: return COLOR_BUS_ROUTES;
		case 412: return COLOR_BUS_ROUTES;
		case 414: return COLOR_BUS_ROUTES;
		case 419: return COLOR_BUS_ROUTES;
		case 420: return COLOR_BUS_ROUTES;
		case 421: return COLOR_BUS_ROUTES;
		case 425: return COLOR_BUS_ROUTES;
		case 429: return COLOR_BUS_ROUTES;
		case 430: return COLOR_BUS_ROUTES;
		case 439: return COLOR_BUS_ROUTES;
		case 440: return COLOR_BUS_ROUTES;
		case 444: return COLOR_BUS_ROUTES;
		case 445: return COLOR_BUS_ROUTES;
		case 452: return COLOR_BUS_ROUTES;
		case 453: return COLOR_BUS_ROUTES;
		case 454: return COLOR_BUS_ROUTES;
		case 456: return COLOR_BUS_ROUTES;
		case 468: return COLOR_BUS_ROUTES;
		case 502: return null;
		case 506: return COLOR_BUS_ROUTES;
		case 555: return null;
		case 697: return COLOR_BUS_ROUTES_SCHOOL;
		case 698: return COLOR_BUS_ROUTES_SCHOOL;
		case 699: return COLOR_BUS_ROUTES_SCHOOL;
		case 703: return COLOR_BUS_ROUTES_SCHOOL;
		case 704: return COLOR_BUS_ROUTES_SCHOOL;
		case 705: return COLOR_BUS_ROUTES_SCHOOL;
		case 706: return COLOR_BUS_ROUTES_SCHOOL;
		case 710: return COLOR_BUS_ROUTES_SCHOOL;
		case 711: return COLOR_BUS_ROUTES_SCHOOL;
		case 712: return COLOR_BUS_ROUTES_SCHOOL;
		case 713: return COLOR_BUS_ROUTES_SCHOOL;
		case 714: return COLOR_BUS_ROUTES_SCHOOL;
		case 715: return COLOR_BUS_ROUTES_SCHOOL;
		case 716: return COLOR_BUS_ROUTES_SCHOOL;
		case 717: return COLOR_BUS_ROUTES_SCHOOL;
		case 718: return COLOR_BUS_ROUTES_SCHOOL;
		case 719: return COLOR_BUS_ROUTES_SCHOOL;
		case 721: return COLOR_BUS_ROUTES_SCHOOL;
		case 724: return COLOR_BUS_ROUTES_SCHOOL;
		case 725: return COLOR_BUS_ROUTES_SCHOOL;
		case 731: return COLOR_BUS_ROUTES_SCHOOL;
		case 732: return COLOR_BUS_ROUTES_SCHOOL;
		case 733: return COLOR_BUS_ROUTES_SCHOOL;
		case 734: return COLOR_BUS_ROUTES_SCHOOL;
		case 735: return COLOR_BUS_ROUTES_SCHOOL;
		case 737: return COLOR_BUS_ROUTES_SCHOOL;
		case 738: return COLOR_BUS_ROUTES_SCHOOL;
		case 739: return COLOR_BUS_ROUTES_SCHOOL;
		case 740: return COLOR_BUS_ROUTES_SCHOOL;
		case 741: return COLOR_BUS_ROUTES_SCHOOL;
		case 742: return COLOR_BUS_ROUTES_SCHOOL;
		case 743: return COLOR_BUS_ROUTES_SCHOOL;
		case 744: return COLOR_BUS_ROUTES_SCHOOL;
		case 745: return COLOR_BUS_ROUTES_SCHOOL;
		case 746: return COLOR_BUS_ROUTES_SCHOOL;
		case 747: return COLOR_BUS_ROUTES_SCHOOL;
		case 751: return COLOR_BUS_ROUTES_SCHOOL;
		case 752: return COLOR_BUS_ROUTES_SCHOOL;
		case 753: return COLOR_BUS_ROUTES_SCHOOL;
		case 754: return COLOR_BUS_ROUTES_SCHOOL;
		case 755: return COLOR_BUS_ROUTES_SCHOOL;
		case 756: return COLOR_BUS_ROUTES_SCHOOL;
		case 757: return COLOR_BUS_ROUTES_SCHOOL;
		case 758: return COLOR_BUS_ROUTES_SCHOOL;
		case 759: return COLOR_BUS_ROUTES_SCHOOL;
		case 760: return COLOR_BUS_ROUTES_SCHOOL;
		case 761: return COLOR_BUS_ROUTES_SCHOOL;
		case 762: return COLOR_BUS_ROUTES_SCHOOL;
		case 763: return COLOR_BUS_ROUTES_SCHOOL;
		case 764: return COLOR_BUS_ROUTES_SCHOOL;
		case 765: return COLOR_BUS_ROUTES_SCHOOL;
		case 766: return COLOR_BUS_ROUTES_SCHOOL;
		case 770: return COLOR_BUS_ROUTES_SCHOOL;
		case 771: return COLOR_BUS_ROUTES_SCHOOL;
		case 773: return COLOR_BUS_ROUTES_SCHOOL;
		case 774: return COLOR_BUS_ROUTES_SCHOOL;
		case 775: return COLOR_BUS_ROUTES_SCHOOL;
		case 776: return COLOR_BUS_ROUTES_SCHOOL;
		case 778: return COLOR_BUS_ROUTES_SCHOOL;
		case 779: return COLOR_BUS_ROUTES_SCHOOL;
		case 780: return COLOR_BUS_ROUTES_SCHOOL;
		case 791: return COLOR_BUS_ROUTES_SCHOOL;
		case 792: return COLOR_BUS_ROUTES_SCHOOL;
		case 795: return COLOR_BUS_ROUTES_SCHOOL;
		case 796: return COLOR_BUS_ROUTES_SCHOOL;
		case 797: return COLOR_BUS_ROUTES_SCHOOL;
		case 798: return COLOR_BUS_ROUTES_SCHOOL;
		case 799: return COLOR_BUS_ROUTES_SCHOOL;
		case 801: return COLOR_BUS_ROUTES_SCHOOL;
		case 802: return COLOR_BUS_ROUTES_SCHOOL;
		case 804: return COLOR_BUS_ROUTES_SCHOOL;
		case 805: return COLOR_BUS_ROUTES_SCHOOL;
		case 807: return COLOR_BUS_ROUTES_SCHOOL;
		case 811: return COLOR_BUS_ROUTES_SCHOOL;
		case 812: return COLOR_BUS_ROUTES_SCHOOL;
		case 813: return COLOR_BUS_ROUTES_SCHOOL;
		case 814: return COLOR_BUS_ROUTES_SCHOOL;
		case 815: return COLOR_BUS_ROUTES_SCHOOL;
		case 816: return COLOR_BUS_ROUTES_SCHOOL;
		case 817: return COLOR_BUS_ROUTES_SCHOOL;
		case 818: return COLOR_BUS_ROUTES_SCHOOL;
		case 819: return COLOR_BUS_ROUTES_SCHOOL;
		case 821: return COLOR_BUS_ROUTES_SCHOOL;
		case 822: return COLOR_BUS_ROUTES_SCHOOL;
		case 830: return COLOR_BUS_ROUTES_SCHOOL;
		case 831: return COLOR_BUS_ROUTES_SCHOOL;
		case 832: return COLOR_BUS_ROUTES_SCHOOL;
		case 834: return COLOR_BUS_ROUTES_SCHOOL;
		case 835: return COLOR_BUS_ROUTES_SCHOOL;
		case 837: return COLOR_BUS_ROUTES_SCHOOL;
		case 838: return COLOR_BUS_ROUTES_SCHOOL;
		case 841: return COLOR_BUS_ROUTES_SCHOOL;
		case 842: return COLOR_BUS_ROUTES_SCHOOL;
		case 851: return COLOR_BUS_ROUTES_SCHOOL;
		case 853: return COLOR_BUS_ROUTES_SCHOOL;
		case 857: return COLOR_BUS_ROUTES_SCHOOL;
		case 860: return COLOR_BUS_ROUTES_SCHOOL;
		case 861: return COLOR_BUS_ROUTES_SCHOOL;
		case 878: return COLOR_BUS_ROUTES_SCHOOL;
		case 880: return COLOR_BUS_ROUTES_SCHOOL;
		case 883: return COLOR_BUS_ROUTES_SCHOOL;
		case 884: return COLOR_BUS_ROUTES_SCHOOL;
		case 888: return COLOR_BUS_ROUTES_SCHOOL;
		case 889: return COLOR_BUS_ROUTES_SCHOOL;
		case 892: return COLOR_BUS_ROUTES_SCHOOL;
		// @formatter:on
		default:
			System.out.println("Unexpected route color " + gRoute);
			System.exit(-1);
			return null;
		}
	}

	private static final String _69_ST_STN = "69 St Stn";
	private static final String ACADIA = "Acadia";
	private static final String OAKRIDGE = "Oakridge";
	private static final String ACADIA_OAKRIDGE = ACADIA + " / " + OAKRIDGE;
	private static final String AIRPORT = "Airport";
	private static final String ANDERSON = "Anderson";
	private static final String ANDERSON_STN = ANDERSON; // "Anderson Stn";
	private static final String ANNIE_GALE = "Annie Gale";
	private static final String APPLEWOOD = "Applewood";
	private static final String ARBOUR_LK = "Arbour Lk";
	private static final String AUBURN_BAY = "Auburn Bay";
	private static final String B_GRANDIN = "B Grandin";
	private static final String BARLOW_STN = "Barlow Stn";
	private static final String BEAVERBROOK = "Beaverbrook";
	private static final String BEDDINGTON = "Beddington";
	private static final String BISHOP_O_BYRNE = "B O'Byrne";
	private static final String BONAVISTA = "Bonavista";
	private static final String BONAVISTA_WEST = "W " + BONAVISTA;
	private static final String BOWNESS = "Bowness";
	private static final String BREBEUF = "Brebeuf";
	private static final String BRENTWOOD = "Brentwood";
	private static final String BRENTWOOD_STN = BRENTWOOD; // "Brentwood Stn";
	private static final String BRIDGELAND = "Bridgeland";
	private static final String CASTLERIDGE = "Castleridge";
	private static final String CENTRAL_MEMORIAL = "Central Memorial";
	private static final String CHAPARRAL = "Chaparral";
	private static final String CHATEAU_ESTS = "Chateau Ests";
	private static final String CHINOOK = "Chinook";
	private static final String CHINOOK_STN = CHINOOK; // "Chinook Stn";
	private static final String CHURCHILL = "Churchill";
	private static final String CIRCLE_ROUTE = "Circle Route";
	private static final String CITADEL = "Citadel";
	private static final String CITY_CTR = "City Ctr";
	private static final String COACH_HL = "Coach Hl";
	private static final String COPPERFIELD = "Copperfield";
	private static final String CORAL_SPGS = "Coral Spgs";
	private static final String COUNTRY_HLS = "Country Hls";
	private static final String COUNTRY_VLG = "Country Vlg";
	private static final String COVENTRY = "Coventry";
	private static final String COVENTRY_HLS = COVENTRY + " Hls";
	private static final String COVENTRY_SOUTH = "S" + COVENTRY;
	private static final String CRANSTON = "Cranston";
	private static final String CRESCENT_HTS = "Crescent Hts";
	private static final String DALHOUSIE = "Dalhousie";
	private static final String DEER_RUN = "Deer Run";
	private static final String DEERFOOT_CTR = "Deerfoot Ctr";
	private static final String DIEFENBAKER = "Diefenbaker";
	private static final String DISCOVERY_RIDGE = "Discovery Rdg";
	private static final String DOUGLASDALE = "Douglasdale";
	private static final String DOUGLAS_GLEN = "Douglas Glen";
	private static final String DOWNTOWN = "Downtown";
	private static final String EDGEBROOK_RISE = "Edgebrook Rise";
	private static final String EDGEMONT = "Edgemont";
	private static final String ELBOW_DR = "Elbow Dr";
	private static final String ERIN_WOODS = "Erin Woods";
	private static final String ERINWOODS = "Erinwoods";
	private static final String EVANSTON = "Evanston";
	private static final String EVERGREEN = "Evergreen";
	private static final String SOMERSET = "Somerset";
	private static final String EVERGREEN_SOMERSET = EVERGREEN + " / " + SOMERSET;
	private static final String F_WHELIHAN = "F Whelihan";
	private static final String FALCONRIDGE = "Falconridge";
	private static final String FOOTHILLS_IND = "Foothills Ind";
	private static final String FOREST_HTS = "Forest Hts";
	private static final String FOREST_LAWN = "Forest Lawn";
	private static final String FOWLER = "Fowler";
	private static final String FRANKLIN = "Franklin";
	private static final String GLAMORGAN = "Glamorgan";
	private static final String GREENWOOD = "Greenwood";
	private static final String HAMPTONS = "Hamptons";
	private static final String HARVEST_HLS = "Harvest Hls";
	private static final String HAWKWOOD = "Hawkwood";
	private static final String HERITAGE = "Heritage";
	private static final String HERITAGE_STN = HERITAGE; // "Heritage Stn";
	private static final String HIDDEN_VLY = "Hidden Vly";
	private static final String HILLHURST = "Hillhurst";
	private static final String HUNTINGTON = "Huntington";
	private static final String KINCORA = "Kincora";
	private static final String LAKEVIEW = "Lakeview";
	private static final String LIONS_PARK = "Lions Park";
	private static final String LIONS_PARK_STN = LIONS_PARK; // "Lions Park Stn";
	private static final String LYNNWOOD = "Lynnwood";
	private static final String M_D_HOUET = "M d'Houet";
	private static final String MAC_EWAN = "MacEwan";
	private static final String MARLBOROUGH = "Marlborough";
	private static final String MARTINDALE = "Martindale";
	private static final String MC_CALL_WAY = "McCall Way";
	private static final String MC_KENZIE = "McKenzie";
	private static final String MC_KENZIE_LK_WAY = MC_KENZIE + " Lk Way";
	private static final String MC_KENZIE_TOWNE = MC_KENZIE + " Towne";
	private static final String MC_KENZIE_TOWNE_DR = MC_KENZIE_TOWNE; // "McKenzie Towne Dr";
	private static final String MC_KINGHT_WESTWINDS = "McKinght-Westwinds";
	private static final String MC_KNIGHT_WESTWINDS = "McKnight-Westwinds";
	private static final String MRU = "MRU";
	private static final String MRU_NORTH = MRU + " North";
	private static final String MRU_SOUTH = MRU + " South";
	private static final String MT_ROYAL_U = MRU; // "Mt Royal U";
	private static final String MTN_PARK = "Mtn Park";
	private static final String NEW_BRIGHTON = "New Brighton";
	private static final String NORTH_HAVEN = "North Haven";
	private static final String NORTH_POINTE = "North Pte";
	private static final String NORTHLAND = "Northland";
	private static final String NORTHMOUNT_DR = "Northmount Dr";
	private static final String NORTHWEST_LOOP = "Northwest Loop";
	private static final String NOTRE_DAME = "Notre Dame";
	private static final String OAKRIDGE_ACADIA = OAKRIDGE + " / " + ACADIA;
	private static final String OGDEN = "Ogden";
	private static final String OGDEN_NORTH = "North " + OGDEN;
	private static final String PALLISER_OAKRIDGE = "Palliser / Oakridge";
	private static final String PANORAMA = "Panorama";
	private static final String PANORAMA_HLS = PANORAMA + " Hls";
	private static final String PANORAMA_HLS_NORTH = "N " + PANORAMA_HLS;
	private static final String PARKHILL_FOOTHILLS = "Parkhill / Foothills";
	private static final String PARKLAND = "Parkland";
	private static final String PRESTWICK = "Prestwick";
	private static final String QUEEN_ELIZABETH = "Queen Elizabeth";
	private static final String QUEENSLAND = "Queensland";
	private static final String R_THIRSK = "R Thirsk";
	private static final String RAMSAY = "Ramsay";
	private static final String RENFREW = "Renfrew";
	private static final String RIVERBEND = "Riverbend";
	private static final String ROCKY_RIDGE = "Rocky Rdg";
	private static final String ROYAL_OAK = "Royal Oak";
	private static final String SADDLECREST = "Saddlecrest";
	private static final String SADDLE_RIDGE = "Saddle Rdg";
	private static final String SADDLETOWN = "Saddletown";
	private static final String SADDLETOWNE = "Saddletowne";
	private static final String SAGE_HILL_KINCORA = "Sage Hill / Kincora";
	private static final String SANDSTONE = "Sandstone";
	private static final String SANDSTONE_AIRPORT = "Sandstone / " + AIRPORT;
	private static final String SARCEE_RD = "Sarcee Rd";
	private static final String SCARLETT = "Scarlett";
	private static final String SCENIC_ACRES = "Scenic Acres";
	private static final String SCENIC_ACRES_SOUTH = "S " + SCENIC_ACRES;
	private static final String SCENIC_ACRES_NORTH = "N " + SCENIC_ACRES;
	private static final String SHAWVILLE = "Shawville";
	private static final String SHERWOOD = "Sherwood";
	private static final String SILVER_SPGS = "Silver Spgs";
	private static final String SKYVIEW_RANCH = "Skyview Ranch";
	private static final String SOMERSET_BRIDLEWOOD_STN = SOMERSET + "-Bridlewood Stn";
	private static final String SOUTH_CALGARY = "South Calgary";
	private static final String SOUTH_HEALTH = "South Health";
	private static final String SOUTHCENTER = "Southcentre";
	private static final String ST_AUGUSTINE = "St Augustine";
	private static final String ST_FRANCIS = "St Francis";
	private static final String ST_ISABELLA = "St Isabella";
	private static final String ST_MARGARET = "St Margaret";
	private static final String ST_MATTHEW = "St Matthew";
	private static final String ST_STEPHEN = "St Stephen";
	private static final String STRATHCONA = "Strathcona";
	private static final String TARADALE = "Taradale";
	private static final String TOM_BAINES = "Tom Baines";
	private static final String TUSCANY = "Tuscany";
	private static final String VALLEY_RIDGE = "Vly Rdg";
	private static final String VARSITY_ACRES = "Varsity Acres";
	private static final String VINCENT_MASSEY = "V Massey";
	private static final String VISTA_HTS = "Vista Hts";
	private static final String WCHS_ST_MARY_S = "WCHS/St Mary''s";
	private static final String WESTBROOK = "Westbrook";
	private static final String WESTBROOK_STN = WESTBROOK + " Stn";
	private static final String WESTERN_CANADA = "Western Canada";
	private static final String WESTGATE = "Westgate";
	private static final String WESTHILLS = "Westhills";
	private static final String WHITEHORN = "Whitehorn";
	private static final String WHITEHORN_STN = WHITEHORN; // WHITEHORN + " Stn";
	private static final String WISE_WOOD = "Wise Wood";
	private static final String WOODBINE = "Woodbine";
	private static final String WOODLANDS = "Woodlands";

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (mRoute.id == 1l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(FOREST_LAWN, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(BOWNESS, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 2l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (mRoute.id == 3l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SANDSTONE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ELBOW_DR, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 4l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(HUNTINGTON, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 5l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(NORTH_HAVEN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 6l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(WESTBROOK_STN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 7l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(SOUTH_CALGARY, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 9l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(BRIDGELAND, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(VARSITY_ACRES, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 10l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(DALHOUSIE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(SOUTHCENTER, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 13l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(WESTHILLS, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 15l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (mRoute.id == 17l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(RENFREW, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(RAMSAY, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 18l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(MT_ROYAL_U, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 19l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (mRoute.id == 20l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(NORTHMOUNT_DR, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(HERITAGE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 22l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(DALHOUSIE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 23l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SADDLETOWNE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(FOOTHILLS_IND, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 24l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 26l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(FRANKLIN, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(MARLBOROUGH, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 30l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (mRoute.id == 33l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(VISTA_HTS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(BARLOW_STN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 37l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(NORTHWEST_LOOP, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 41l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(LYNNWOOD, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 49l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(FOREST_HTS, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 52l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(EVERGREEN_SOMERSET, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 55l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(FALCONRIDGE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 57l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(MC_CALL_WAY, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ERINWOODS, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 62l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(HIDDEN_VLY, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			}

		} else if (mRoute.id == 63l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(LAKEVIEW, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 64l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(MAC_EWAN, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 66l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SADDLETOWNE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CHINOOK, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 69l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(DEERFOOT_CTR, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 70l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(VALLEY_RIDGE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 71l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SADDLETOWNE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(MC_KINGHT_WESTWINDS, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 72l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CIRCLE_ROUTE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 73l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CIRCLE_ROUTE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 74l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(TUSCANY, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 79l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(ACADIA_OAKRIDGE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 80l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(OAKRIDGE_ACADIA, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 81l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (mRoute.id == 85l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SADDLETOWNE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(MC_KNIGHT_WESTWINDS, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 86l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (mRoute.id == 91l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(LIONS_PARK_STN, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(BRENTWOOD_STN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 92l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(ANDERSON_STN, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(MC_KENZIE_TOWNE_DR, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 93l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(WESTBROOK, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(COACH_HL, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 94l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(_69_ST_STN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 98l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(_69_ST_STN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 100l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(AIRPORT, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(MC_KNIGHT_WESTWINDS, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 102l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOUGLASDALE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 103l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(MC_KENZIE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 107l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(SOUTH_CALGARY, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 109l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(HARVEST_HLS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 110l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 112l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(SARCEE_RD, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 116l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(COVENTRY_HLS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 117l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(MC_KENZIE_TOWNE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 125l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(ERIN_WOODS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 126l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(APPLEWOOD, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 133l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CRANSTON, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 142l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(PANORAMA, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 145l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(NORTHLAND, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 151l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(NEW_BRIGHTON, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 152l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(NEW_BRIGHTON, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 158l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(ROYAL_OAK, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 174l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(TUSCANY, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 176l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (mRoute.id == 178l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CHAPARRAL, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 181l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(MRU_NORTH, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 182l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(MRU_SOUTH, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 300l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(AIRPORT, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 301l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(COUNTRY_VLG, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 302l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(SOUTH_HEALTH, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 305l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (mRoute.id == 306l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(WESTBROOK, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(HERITAGE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 405l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(BRENTWOOD, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(HILLHURST, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 406l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(MC_KENZIE_TOWNE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(SHAWVILLE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 407l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(BRENTWOOD, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(GREENWOOD, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 408l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(BRENTWOOD, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(VALLEY_RIDGE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 411l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 412l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(WESTGATE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 419l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(PARKHILL_FOOTHILLS, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 425l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SAGE_HILL_KINCORA, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 430l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SANDSTONE_AIRPORT, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 439l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(DISCOVERY_RIDGE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 440l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CHATEAU_ESTS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(FRANKLIN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 445l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SKYVIEW_RANCH, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(SADDLETOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 697l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(EVANSTON, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 698l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(WCHS_ST_MARY_S, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(_69_ST_STN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 699l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (mRoute.id == 703l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SHERWOOD, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CHURCHILL, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 704l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(COUNTRY_HLS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CHURCHILL, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 705l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(EDGEBROOK_RISE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CHURCHILL, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 706l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(HAMPTONS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CHURCHILL, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 710l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(BEAVERBROOK, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CRANSTON, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 711l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(BEAVERBROOK, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOUGLAS_GLEN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 712l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(BEAVERBROOK, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(PARKLAND, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 713l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(BEAVERBROOK, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DEER_RUN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 714l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(BEAVERBROOK, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(PRESTWICK, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 715l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(BEAVERBROOK, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(QUEENSLAND, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 716l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(BEAVERBROOK, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(NEW_BRIGHTON, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 717l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(BEAVERBROOK, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(COPPERFIELD, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 718l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(BEAVERBROOK, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOUGLASDALE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 719l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(BEAVERBROOK, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(MC_KENZIE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 721l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(TUSCANY, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(BOWNESS, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 724l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(TUSCANY, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(BOWNESS, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 725l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SILVER_SPGS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(BOWNESS, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 731l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(RIVERBEND, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CENTRAL_MEMORIAL, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 732l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CENTRAL_MEMORIAL, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(GLAMORGAN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 733l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CENTRAL_MEMORIAL, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(LAKEVIEW, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 734l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(OGDEN, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CENTRAL_MEMORIAL, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 735l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(OGDEN_NORTH, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CENTRAL_MEMORIAL, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 737l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(HARVEST_HLS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DIEFENBAKER, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 738l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(PANORAMA_HLS_NORTH, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DIEFENBAKER, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 739l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(PANORAMA_HLS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DIEFENBAKER, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 740l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SADDLETOWNE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CRESCENT_HTS, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 741l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SADDLECREST, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CRESCENT_HTS, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 742l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SADDLE_RIDGE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CRESCENT_HTS, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 743l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(WHITEHORN_STN, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CRESCENT_HTS, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 744l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(COVENTRY_SOUTH, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CRESCENT_HTS, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 745l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(VISTA_HTS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CRESCENT_HTS, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 746l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(COVENTRY_HLS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CRESCENT_HTS, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 747l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(HIDDEN_VLY, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CRESCENT_HTS, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 751l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(TARADALE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(FOWLER, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 752l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(MARTINDALE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(FOWLER, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 753l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(EVANSTON, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 754l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SADDLETOWNE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(FOWLER, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 755l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CASTLERIDGE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(FOWLER, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 756l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(MARTINDALE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(FOWLER, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 757l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CORAL_SPGS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(FOWLER, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 758l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(TARADALE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(FOWLER, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 759l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(FALCONRIDGE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(FOWLER, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 760l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(BONAVISTA_WEST, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(SCARLETT, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 761l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SCARLETT, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(AUBURN_BAY, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 762l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(BONAVISTA, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(SCARLETT, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 763l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SCARLETT, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(WOODBINE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 764l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SCARLETT, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(SOMERSET_BRIDLEWOOD_STN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 765l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SCARLETT, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(SOMERSET_BRIDLEWOOD_STN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 766l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SCARLETT, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(EVERGREEN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 770l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(WESTERN_CANADA, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(OGDEN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 771l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CHINOOK_STN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 773l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(R_THIRSK, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ROCKY_RIDGE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 774l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(R_THIRSK, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ROYAL_OAK, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 775l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CITADEL, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(R_THIRSK, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 776l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(WISE_WOOD, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(PALLISER_OAKRIDGE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 778l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(WISE_WOOD, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(WOODLANDS, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 779l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(WISE_WOOD, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(WOODBINE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 780l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(WISE_WOOD, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(OAKRIDGE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 791l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(MAC_EWAN, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(QUEEN_ELIZABETH, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 792l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SANDSTONE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(QUEEN_ELIZABETH, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 795l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(VINCENT_MASSEY, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(STRATHCONA, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 796l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(EDGEMONT, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(TOM_BAINES, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 798l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(TARADALE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ANNIE_GALE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 799l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CORAL_SPGS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ANNIE_GALE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 801l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(BREBEUF, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ROYAL_OAK, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 802l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(BREBEUF, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(HAWKWOOD, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 804l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SHERWOOD, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(BREBEUF, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 805l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(HAMPTONS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(BREBEUF, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 807l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(BREBEUF, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ROCKY_RIDGE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 811l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(ST_FRANCIS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(TUSCANY, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 812l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(ST_FRANCIS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CITADEL, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 813l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(ST_FRANCIS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ARBOUR_LK, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 814l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(ST_FRANCIS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ROYAL_OAK, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 815l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(ST_FRANCIS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ARBOUR_LK, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 816l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(ST_FRANCIS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CITADEL, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 817l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(ST_FRANCIS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ROCKY_RIDGE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 818l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(HAMPTONS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ST_FRANCIS, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 819l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SHERWOOD, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ST_FRANCIS, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 821l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(MTN_PARK, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(BISHOP_O_BYRNE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 822l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(MC_KENZIE_LK_WAY, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(BISHOP_O_BYRNE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 830l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SANDSTONE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(M_D_HOUET, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 831l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(ST_FRANCIS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(SCENIC_ACRES_NORTH, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 832l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(ST_FRANCIS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(SCENIC_ACRES_SOUTH, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 834l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(DALHOUSIE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(M_D_HOUET, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 835l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(ANDERSON, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 837l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(SCENIC_ACRES_SOUTH, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 838l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(SCENIC_ACRES_NORTH, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 841l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(NOTRE_DAME, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(HIDDEN_VLY, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 842l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(NOTRE_DAME, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(MAC_EWAN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 851l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(LYNNWOOD, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ST_AUGUSTINE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 853l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(RIVERBEND, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ST_AUGUSTINE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 857l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(ST_STEPHEN, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(EVERGREEN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 860l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(B_GRANDIN, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CRANSTON, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 861l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(B_GRANDIN, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(AUBURN_BAY, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 878l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(F_WHELIHAN, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(CHAPARRAL, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 880l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(ST_MATTHEW, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(HERITAGE_STN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 883l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(EVANSTON, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 884l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(KINCORA, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 888l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(NORTH_POINTE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ST_MARGARET, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 889l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(BEDDINGTON, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 892l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(ST_ISABELLA, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(MC_KENZIE, gTrip.direction_id);
				return;
			}
		}

		mTrip.setHeadsignString(cleanTripHeadsign(gTrip.trip_headsign.toLowerCase(Locale.ENGLISH)), gTrip.direction_id);
	}

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = tripHeadsign.toLowerCase(Locale.ENGLISH);
		return MSpec.cleanLabel(tripHeadsign);
	}

	private static final Pattern ENDS_WITH_BOUND = Pattern.compile("([\\s]*[s|e|w|n]b[\\s]$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_BOUND = Pattern.compile("(^[\\s]*[s|e|w|n]b[\\s]*)", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_SLASH = Pattern.compile("(^[\\s]*/[\\s]*)", Pattern.CASE_INSENSITIVE);

	private static final String REGEX_START_END = "((^|[^A-Z]){1}(%s)([^a-zA-Z]|$){1})";
	private static final String REGEX_START_END_REPLACEMENT = "$2 %s $4";

	private static final Pattern AT_SIGN = Pattern.compile("([\\s]*@[\\s]*)", Pattern.CASE_INSENSITIVE);
	private static final String AT_SIGN_REPLACEMENT = " / ";

	private static final Pattern STATION = Pattern.compile(String.format(REGEX_START_END, "station"), Pattern.CASE_INSENSITIVE);
	private static final String STATION_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Stn");

	private static final Pattern AV = Pattern.compile(String.format(REGEX_START_END, "AV|AVE"));
	private static final String AV_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Avenue");

	private static final Pattern PA = Pattern.compile(String.format(REGEX_START_END, "PA"));
	private static final String PA_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Park");

	private static final Pattern HT = Pattern.compile(String.format(REGEX_START_END, "HT"));
	private static final String HT_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Heights");

	private static final Pattern GV = Pattern.compile(String.format(REGEX_START_END, "GV"));
	private static final String GV_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Grove");

	private static final Pattern PT = Pattern.compile(String.format(REGEX_START_END, "PT"));
	private static final String PT_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Point");

	private static final Pattern TC = Pattern.compile(String.format(REGEX_START_END, "TC"));
	private static final String TC_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Terrace");

	private static final Pattern RI = Pattern.compile(String.format(REGEX_START_END, "RI"));
	private static final String RI_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Rise");

	private static final Pattern MR = Pattern.compile(String.format(REGEX_START_END, "MR"));
	private static final String MR_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Manor");

	private static final Pattern DR = Pattern.compile(String.format(REGEX_START_END, "DR"));
	private static final String DR_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Drive");

	private static final Pattern ST = Pattern.compile(String.format(REGEX_START_END, "ST"));
	private static final String ST_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Street");

	private static final Pattern VI = Pattern.compile(String.format(REGEX_START_END, "VI"));
	private static final String VI_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Villas");

	private static final Pattern PZ = Pattern.compile(String.format(REGEX_START_END, "PZ"));
	private static final String PZ_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Plaza");

	private static final Pattern WY = Pattern.compile(String.format(REGEX_START_END, "WY"));
	private static final String WY_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Way");

	private static final Pattern GR = Pattern.compile(String.format(REGEX_START_END, "GR"));
	private static final String GR_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Green");

	private static final Pattern BV = Pattern.compile(String.format(REGEX_START_END, "BV"));
	private static final String BV_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Boulevard");

	private static final Pattern GA = Pattern.compile(String.format(REGEX_START_END, "GA"));
	private static final String GA_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Gate");

	private static final Pattern RD = Pattern.compile(String.format(REGEX_START_END, "RD"));
	private static final String RD_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Road");

	private static final Pattern LI = Pattern.compile(String.format(REGEX_START_END, "LI|LINK"));
	private static final String LI_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Link");

	private static final Pattern PL = Pattern.compile(String.format(REGEX_START_END, "PL"));
	private static final String PL_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Place");

	private static final Pattern SQ = Pattern.compile(String.format(REGEX_START_END, "SQ"));
	private static final String SQ_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Square");

	private static final Pattern CL = Pattern.compile(String.format(REGEX_START_END, "CL"));
	private static final String CL_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Close");

	private static final Pattern CR = Pattern.compile(String.format(REGEX_START_END, "CR"));
	private static final String CR_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Crescent");

	private static final Pattern GD = Pattern.compile(String.format(REGEX_START_END, "GD"));
	private static final String GD_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Gardens");

	private static final Pattern LN = Pattern.compile(String.format(REGEX_START_END, "LN"));
	private static final String LN_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Lane");

	private static final Pattern CO = Pattern.compile(String.format(REGEX_START_END, "CO"));
	private static final String CO_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Ct");

	private static final Pattern CI = Pattern.compile(String.format(REGEX_START_END, "CI"));
	private static final String CI_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Circle");

	private static final Pattern HE = Pattern.compile(String.format(REGEX_START_END, "HE"));
	private static final String HE_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Heath");

	private static final Pattern ME = Pattern.compile(String.format(REGEX_START_END, "ME"));
	private static final String ME_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Mews");

	private static final Pattern TR = Pattern.compile(String.format(REGEX_START_END, "TR"));
	private static final String TR_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Trail");

	private static final Pattern LD = Pattern.compile(String.format(REGEX_START_END, "LD"));
	private static final String LD_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Landing");

	private static final Pattern HL = Pattern.compile(String.format(REGEX_START_END, "HL"));
	private static final String HL_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Hill");

	private static final Pattern PK = Pattern.compile(String.format(REGEX_START_END, "PK"));
	private static final String PK_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Park");

	private static final Pattern CM = Pattern.compile(String.format(REGEX_START_END, "CM"));
	private static final String CM_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Common");

	private static final Pattern GT = Pattern.compile(String.format(REGEX_START_END, "GT"));
	private static final String GT_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Gate");

	private static final Pattern CV = Pattern.compile(String.format(REGEX_START_END, "CV"));
	private static final String CV_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Cove");

	private static final Pattern VW = Pattern.compile(String.format(REGEX_START_END, "VW"));
	private static final String VW_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "View");

	private static final Pattern BY = Pattern.compile(String.format(REGEX_START_END, "BY|BA|BAY"));
	private static final String BY_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Bay");

	private static final Pattern CE = Pattern.compile(String.format(REGEX_START_END, "CE"));
	private static final String CE_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Center");

	private static final Pattern CTR = Pattern.compile(String.format(REGEX_START_END, "CTR"));
	private static final String CTR_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Center");

	private static final Pattern MOUNT_ROYAL_UNIVERSITY = Pattern.compile(String.format(REGEX_START_END, "Mount Royal University"));
	private static final String MOUNT_ROYAL_UNIVERSITY_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "MRU");

	private static final Pattern MOUNT = Pattern.compile(String.format(REGEX_START_END, "Mount"));
	private static final String MOUNT_REPLACEMENT = String.format(REGEX_START_END_REPLACEMENT, "Mt");

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = STARTS_WITH_BOUND.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		gStopName = ENDS_WITH_BOUND.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		gStopName = AT_SIGN.matcher(gStopName).replaceAll(AT_SIGN_REPLACEMENT);
		gStopName = AV.matcher(gStopName).replaceAll(AV_REPLACEMENT);
		gStopName = PA.matcher(gStopName).replaceAll(PA_REPLACEMENT);
		gStopName = HT.matcher(gStopName).replaceAll(HT_REPLACEMENT);
		gStopName = GV.matcher(gStopName).replaceAll(GV_REPLACEMENT);
		gStopName = PT.matcher(gStopName).replaceAll(PT_REPLACEMENT);
		gStopName = TC.matcher(gStopName).replaceAll(TC_REPLACEMENT);
		gStopName = RI.matcher(gStopName).replaceAll(RI_REPLACEMENT);
		gStopName = MR.matcher(gStopName).replaceAll(MR_REPLACEMENT);
		gStopName = DR.matcher(gStopName).replaceAll(DR_REPLACEMENT);
		gStopName = ST.matcher(gStopName).replaceAll(ST_REPLACEMENT);
		gStopName = VI.matcher(gStopName).replaceAll(VI_REPLACEMENT);
		gStopName = PZ.matcher(gStopName).replaceAll(PZ_REPLACEMENT);
		gStopName = WY.matcher(gStopName).replaceAll(WY_REPLACEMENT);
		gStopName = GR.matcher(gStopName).replaceAll(GR_REPLACEMENT);
		gStopName = BV.matcher(gStopName).replaceAll(BV_REPLACEMENT);
		gStopName = GA.matcher(gStopName).replaceAll(GA_REPLACEMENT);
		gStopName = RD.matcher(gStopName).replaceAll(RD_REPLACEMENT);
		gStopName = LI.matcher(gStopName).replaceAll(LI_REPLACEMENT);
		gStopName = PL.matcher(gStopName).replaceAll(PL_REPLACEMENT);
		gStopName = SQ.matcher(gStopName).replaceAll(SQ_REPLACEMENT);
		gStopName = CL.matcher(gStopName).replaceAll(CL_REPLACEMENT);
		gStopName = CR.matcher(gStopName).replaceAll(CR_REPLACEMENT);
		gStopName = GD.matcher(gStopName).replaceAll(GD_REPLACEMENT);
		gStopName = LN.matcher(gStopName).replaceAll(LN_REPLACEMENT);
		gStopName = CO.matcher(gStopName).replaceAll(CO_REPLACEMENT);
		gStopName = ME.matcher(gStopName).replaceAll(ME_REPLACEMENT);
		gStopName = TR.matcher(gStopName).replaceAll(TR_REPLACEMENT);
		gStopName = CI.matcher(gStopName).replaceAll(CI_REPLACEMENT);
		gStopName = HE.matcher(gStopName).replaceAll(HE_REPLACEMENT);
		gStopName = LD.matcher(gStopName).replaceAll(LD_REPLACEMENT);
		gStopName = HL.matcher(gStopName).replaceAll(HL_REPLACEMENT);
		gStopName = PK.matcher(gStopName).replaceAll(PK_REPLACEMENT);
		gStopName = CM.matcher(gStopName).replaceAll(CM_REPLACEMENT);
		gStopName = GT.matcher(gStopName).replaceAll(GT_REPLACEMENT);
		gStopName = CV.matcher(gStopName).replaceAll(CV_REPLACEMENT);
		gStopName = VW.matcher(gStopName).replaceAll(VW_REPLACEMENT);
		gStopName = BY.matcher(gStopName).replaceAll(BY_REPLACEMENT);
		gStopName = CE.matcher(gStopName).replaceAll(CE_REPLACEMENT);
		gStopName = CTR.matcher(gStopName).replaceAll(CTR_REPLACEMENT);
		gStopName = MOUNT_ROYAL_UNIVERSITY.matcher(gStopName).replaceAll(MOUNT_ROYAL_UNIVERSITY_REPLACEMENT);
		gStopName = MOUNT.matcher(gStopName).replaceAll(MOUNT_REPLACEMENT);
		gStopName = STATION.matcher(gStopName).replaceAll(STATION_REPLACEMENT);
		gStopName = MSpec.cleanStreetTypes(gStopName);
		gStopName = MSpec.cleanNumbers(gStopName);
		gStopName = STARTS_WITH_SLASH.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		return MSpec.cleanLabel(gStopName);
	}
}
