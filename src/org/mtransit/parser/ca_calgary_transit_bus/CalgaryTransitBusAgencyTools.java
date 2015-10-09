package org.mtransit.parser.ca_calgary_transit_bus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Pair;
import org.mtransit.parser.SplitUtils;
import org.mtransit.parser.SplitUtils.RouteTripSpec;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.gtfs.data.GTripStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirectionType;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

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
		System.out.printf("\nGenerating Calgary Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("\nGenerating Calgary Transit bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
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

	private static final String RSN_FLOATER = "FLT";
	private static final long RID_FLOATER = 10001l;

	@Override
	public long getRouteId(GRoute gRoute) {
		if (!Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			if (RSN_FLOATER.equals(gRoute.getRouteShortName())) {
				return RID_FLOATER;
			}
		}
		return Long.parseLong(gRoute.getRouteShortName()); // using route short name as route ID
	}

	private static final Pattern CLEAN_STREET_POINT = Pattern.compile("((\\s)*(ave|st|mt)\\.(\\s)*)", Pattern.CASE_INSENSITIVE);
	private static final String CLEAN_AVE_POINT_REPLACEMENT = "$2$3$4";

	@Override
	public String getRouteLongName(GRoute gRoute) {
		String gRouteLongName = gRoute.getRouteLongName();
		gRouteLongName = CleanUtils.cleanSlashes(gRouteLongName);
		gRouteLongName = CLEAN_STREET_POINT.matcher(gRouteLongName).replaceAll(CLEAN_AVE_POINT_REPLACEMENT);
		gRouteLongName = CleanUtils.cleanStreetTypes(gRouteLongName);
		return CleanUtils.cleanLabel(gRouteLongName);
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
		if (!Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			if (RSN_FLOATER.equals(gRoute.getRouteShortName())) {
				return null;
			}
		}
		int rsn = Integer.parseInt(gRoute.getRouteShortName());
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
		case 304: return COLOR_BUS_ROUTES_BRT;
		case 305: return COLOR_BUS_ROUTES_BRT;
		case 306: return COLOR_BUS_ROUTES_BRT;
		case 308: return COLOR_BUS_ROUTES_BRT;
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
		case 696: return COLOR_BUS_ROUTES_SCHOOL;
		case 697: return COLOR_BUS_ROUTES_SCHOOL;
		case 698: return COLOR_BUS_ROUTES_SCHOOL;
		case 699: return COLOR_BUS_ROUTES_SCHOOL;
		case 702: return COLOR_BUS_ROUTES_SCHOOL;
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
		case 777: return COLOR_BUS_ROUTES_SCHOOL;
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
		case 803: return COLOR_BUS_ROUTES_SCHOOL;
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
		case 833: return COLOR_BUS_ROUTES_SCHOOL;
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
			System.out.printf("\nUnexpected route color %s!\n", gRoute);
			System.exit(-1);
			return null;
		}
	}

	private static final String _69_ST_STATION = "69 St Sta";
	private static final String ACADIA = "Acadia";
	private static final String OAKRIDGE = "Oakridge";
	private static final String ACADIA_OAKRIDGE = ACADIA + " / " + OAKRIDGE;
	private static final String AIRPORT = "Airport";
	private static final String ANDERSON = "Anderson";
	private static final String ANDERSON_STATION = ANDERSON; // "Anderson Sta";
	private static final String ANNIE_GALE = "Annie Gale";
	private static final String APPLEWOOD = "Applewood";
	private static final String ARBOUR_LK = "Arbour Lk";
	private static final String AUBURN_BAY = "Auburn Bay";
	private static final String B_GRANDIN = "B Grandin";
	private static final String BARLOW_STATION = "Barlow Sta";
	private static final String BEAVERBROOK = "Beaverbrook";
	private static final String BEDDINGTON = "Beddington";
	private static final String BISHOP_O_BYRNE = "B O'Byrne";
	private static final String BONAVISTA = "Bonavista";
	private static final String BONAVISTA_WEST = "W " + BONAVISTA;
	private static final String BOWNESS = "Bowness";
	private static final String BREBEUF = "Brebeuf";
	private static final String BRENTWOOD = "Brentwood";
	private static final String BRENTWOOD_STATION = BRENTWOOD; // "Brentwood Sta";
	private static final String BRIDGELAND = "Bridgeland";
	private static final String CASTLERIDGE = "Castleridge";
	private static final String CENTRAL_MEMORIAL = "Central Memorial";
	private static final String CHAPARRAL = "Chaparral";
	private static final String CHATEAU_ESTS = "Chateau Ests";
	private static final String CHINOOK = "Chinook";
	private static final String CHINOOK_STATION = CHINOOK; // "Chinook Sta";
	private static final String CHURCHILL = "Churchill";
	private static final String CIRCLE_ROUTE = "Circle Route";
	private static final String CITADEL = "Citadel";
	private static final String CITY_CTR = "City Ctr";
	private static final String COACH_HL = "Coach Hl";
	private static final String COPPERFIELD = "Copperfield";
	private static final String CORAL_SPGS = "Coral Spgs";
	private static final String COUGAR_RDG = "Cougar Rdg";
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
	private static final String FOOTHILLS = "Foothills";
	private static final String FOOTHILLS_IND = FOOTHILLS + " Ind";
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
	private static final String HERITAGE_STATION = HERITAGE; // "Heritage Sta";
	private static final String HIDDEN_VLY = "Hidden Vly";
	private static final String HILLHURST = "Hillhurst";
	private static final String HUNTINGTON = "Huntington";
	private static final String KINCORA = "Kincora";
	private static final String LAKEVIEW = "Lakeview";
	private static final String LIONS_PARK = "Lions Park";
	private static final String LIONS_PARK_STATION = LIONS_PARK; // "Lions Park Sta";
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
	private static final String PARKHILL = "Parkhill";
	private static final String PARKLAND = "Parkland";
	private static final String PARK_GATE_HERITAGE = "Pk Gt Heritage";
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
	private static final String SOMERSET_BRIDLEWOOD_STATION = SOMERSET + "-Bridlewood Sta";
	private static final String SOUTH_CALGARY = "South Calgary";
	private static final String SOUTH_HEALTH = "South Health";
	private static final String SOUTHCENTER = "Southcentre";
	private static final String SOUTHLAND = "Southland";
	private static final String SOUTHLAND_STATION = SOUTHLAND; // "Southland Sta";
	private static final String ST_AUGUSTINE = "St Augustine";
	private static final String ST_FRANCIS = "St Francis";
	private static final String ST_ISABELLA = "St Isabella";
	private static final String ST_MARGARET = "St Margaret";
	private static final String ST_MATTHEW = "St Matthew";
	private static final String ST_STEPHEN = "St Stephen";
	private static final String STATION_HERITAGE = "Sta Heritage";
	private static final String STRATHCONA = "Strathcona";
	private static final String TARADALE = "Taradale";
	private static final String TOM_BAINES = "Tom Baines";
	private static final String TUSCANY = "Tuscany";
	private static final String VALLEY_RIDGE = "Vly Rdg";
	private static final String VARSITY_ACRES = "Varsity Acres";
	private static final String VINCENT_MASSEY = "V Massey";
	private static final String VISTA_HTS = "Vista Hts";
	private static final String WCHS_ST_MARY_S = "WCHS / St Mary''s";
	private static final String WESTBROOK = "Westbrook";
	private static final String WESTBROOK_STATION = WESTBROOK + " Sta";
	private static final String WESTERN_CANADA = "Western Canada";
	private static final String WESTGATE = "Westgate";
	private static final String WESTHILLS = "Westhills";
	private static final String WHITEHORN = "Whitehorn";
	private static final String WHITEHORN_STATION = WHITEHORN; // WHITEHORN + " Sta";
	private static final String WISE_WOOD = "Wise Wood";
	private static final String WOODBINE = "Woodbine";
	private static final String WOODLANDS = "Woodlands";

	private static HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;
	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<Long, RouteTripSpec>();
		map2.put(56l, new RouteTripSpec(56l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, SOUTHLAND_STATION, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, ANDERSON_STATION) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "6461", "6097" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "6097", "6562", "6461" })) //
				.compileBothTripSort());
		map2.put(94l, new RouteTripSpec(94l, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, STRATHCONA, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, WESTBROOK_STATION) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { "3741", "5315", "8379", "6515" })) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { "6515", "3732", "7597", "3741" })) //
				.compileBothTripSort());
		map2.put(98l, new RouteTripSpec(98l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, COUGAR_RDG, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, _69_ST_STATION) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "8374", "8822" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "8822", "8373" })) //
				.compileBothTripSort());
		map2.put(419l, new RouteTripSpec(419l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, FOOTHILLS, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, PARKHILL) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "5574", "5299", "5227", "8339" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "8339", "5108", "5580", "5574" })) //
				.compileBothTripSort());
		map2.put(439l, new RouteTripSpec(439l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, _69_ST_STATION, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, DISCOVERY_RIDGE) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "9365", "3785" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "3785", "9365" })) //
				.compileBothTripSort());
		map2.put(502l, new RouteTripSpec(502l, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, STATION_HERITAGE, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, PARK_GATE_HERITAGE) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { "7592", "5192", "5762" })) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { "5762", "4577", "7592" })) //
				.compileBothTripSort());
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public int compareEarly(long routeId, List<MTripStop> list1, List<MTripStop> list2, MTripStop ts1, MTripStop ts2, GStop ts1GStop, GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@Override
	public ArrayList<MTrip> splitTrip(MRoute mRoute, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return ALL_ROUTE_TRIPS2.get(mRoute.getId()).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@Override
	public Pair<Long[], Integer[]> splitTripStop(MRoute mRoute, GTrip gTrip, GTripStop gTripStop, ArrayList<MTrip> splitTrips, GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()));
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}


	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return; // split
		}
		if (mRoute.getId() == 1l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(FOREST_LAWN, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(BOWNESS, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 2l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (mRoute.getId() == 3l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(SANDSTONE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(ELBOW_DR, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 4l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(HUNTINGTON, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 5l) {
			if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(NORTH_HAVEN, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 6l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(WESTBROOK_STATION, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 7l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(SOUTH_CALGARY, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 9l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BRIDGELAND, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(VARSITY_ACRES, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 10l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(DALHOUSIE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(SOUTHCENTER, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 13l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(WESTHILLS, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 15l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (mRoute.getId() == 17l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(RENFREW, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(RAMSAY, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 18l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(MT_ROYAL_U, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 19l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (mRoute.getId() == 20l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(NORTHMOUNT_DR, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(HERITAGE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 22l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(DALHOUSIE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 23l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(SADDLETOWNE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(FOOTHILLS_IND, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 24l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 26l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(FRANKLIN, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(MARLBOROUGH, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 30l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (mRoute.getId() == 33l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(VISTA_HTS, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(BARLOW_STATION, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 37l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(NORTHWEST_LOOP, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 41l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(LYNNWOOD, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 49l) {
			if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(FOREST_HTS, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 52l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(EVERGREEN_SOMERSET, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 55l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(FALCONRIDGE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 57l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(MC_CALL_WAY, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(ERINWOODS, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 62l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(HIDDEN_VLY, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 63l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(LAKEVIEW, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 64l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(MAC_EWAN, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 66l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(SADDLETOWNE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CHINOOK, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 69l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(DEERFOOT_CTR, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 70l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(VALLEY_RIDGE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 71l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(SADDLETOWNE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(MC_KINGHT_WESTWINDS, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 72l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(CIRCLE_ROUTE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 73l) {
			if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CIRCLE_ROUTE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 74l) {
			if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(TUSCANY, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 79l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(ACADIA_OAKRIDGE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 80l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(OAKRIDGE_ACADIA, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 81l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (mRoute.getId() == 85l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(SADDLETOWNE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(MC_KNIGHT_WESTWINDS, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 86l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (mRoute.getId() == 91l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(LIONS_PARK_STATION, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(BRENTWOOD_STATION, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 92l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(ANDERSON_STATION, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(MC_KENZIE_TOWNE_DR, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 93l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(WESTBROOK, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(COACH_HL, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 94l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(_69_ST_STATION, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 96l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (mRoute.getId() == 98l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(_69_ST_STATION, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 100l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(AIRPORT, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(MC_KNIGHT_WESTWINDS, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 102l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(DOUGLASDALE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 103l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(MC_KENZIE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 107l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(SOUTH_CALGARY, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 109l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(HARVEST_HLS, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 110l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 112l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(SARCEE_RD, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 116l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(COVENTRY_HLS, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 117l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(MC_KENZIE_TOWNE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 125l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(ERIN_WOODS, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 126l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(APPLEWOOD, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 133l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CRANSTON, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 142l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(PANORAMA, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 145l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(NORTHLAND, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 151l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(NEW_BRIGHTON, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 152l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(NEW_BRIGHTON, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 157l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (mRoute.getId() == 158l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(ROYAL_OAK, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 174l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(TUSCANY, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 176l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (mRoute.getId() == 178l) {
			if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CHAPARRAL, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 181l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(MRU_NORTH, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 182l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(MRU_SOUTH, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 300l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(AIRPORT, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 301l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(COUNTRY_VLG, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 302l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(SOUTH_HEALTH, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 305l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (mRoute.getId() == 306l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(WESTBROOK, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(HERITAGE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 405l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BRENTWOOD, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(HILLHURST, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 406l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(MC_KENZIE_TOWNE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(SHAWVILLE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 407l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BRENTWOOD, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(GREENWOOD, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 408l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BRENTWOOD, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(VALLEY_RIDGE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 411l) {
			if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CITY_CTR, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 412l) {
			if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(WESTGATE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 425l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(SAGE_HILL_KINCORA, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 430l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(SANDSTONE_AIRPORT, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 439l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(DISCOVERY_RIDGE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 440l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(CHATEAU_ESTS, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(FRANKLIN, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 445l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(SKYVIEW_RANCH, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(SADDLETOWN, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 697l) {
			if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(EVANSTON, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 698l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(WCHS_ST_MARY_S, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(_69_ST_STATION, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 699l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (mRoute.getId() == 702l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (mRoute.getId() == 703l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(SHERWOOD, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CHURCHILL, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 704l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(COUNTRY_HLS, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CHURCHILL, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 705l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(EDGEBROOK_RISE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CHURCHILL, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 706l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(HAMPTONS, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CHURCHILL, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 710l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BEAVERBROOK, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CRANSTON, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 711l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BEAVERBROOK, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(DOUGLAS_GLEN, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 712l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BEAVERBROOK, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(PARKLAND, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 713l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BEAVERBROOK, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(DEER_RUN, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 714l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BEAVERBROOK, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(PRESTWICK, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 715l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BEAVERBROOK, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(QUEENSLAND, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 716l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BEAVERBROOK, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(NEW_BRIGHTON, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 717l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BEAVERBROOK, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(COPPERFIELD, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 718l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BEAVERBROOK, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(DOUGLASDALE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 719l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BEAVERBROOK, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(MC_KENZIE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 721l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(TUSCANY, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(BOWNESS, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 724l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(TUSCANY, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(BOWNESS, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 725l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(SILVER_SPGS, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(BOWNESS, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 731l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(RIVERBEND, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CENTRAL_MEMORIAL, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 732l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(CENTRAL_MEMORIAL, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(GLAMORGAN, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 733l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(CENTRAL_MEMORIAL, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(LAKEVIEW, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 734l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(OGDEN, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CENTRAL_MEMORIAL, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 735l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(OGDEN_NORTH, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CENTRAL_MEMORIAL, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 737l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(HARVEST_HLS, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(DIEFENBAKER, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 738l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(PANORAMA_HLS_NORTH, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(DIEFENBAKER, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 739l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(PANORAMA_HLS, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(DIEFENBAKER, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 740l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(SADDLETOWNE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CRESCENT_HTS, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 741l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(SADDLECREST, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CRESCENT_HTS, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 742l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(SADDLE_RIDGE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CRESCENT_HTS, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 743l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(WHITEHORN_STATION, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CRESCENT_HTS, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 744l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(COVENTRY_SOUTH, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CRESCENT_HTS, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 745l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(VISTA_HTS, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CRESCENT_HTS, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 746l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(COVENTRY_HLS, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CRESCENT_HTS, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 747l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(HIDDEN_VLY, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CRESCENT_HTS, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 751l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(TARADALE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(FOWLER, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 752l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(MARTINDALE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(FOWLER, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 753l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(EVANSTON, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 754l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(SADDLETOWNE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(FOWLER, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 755l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(CASTLERIDGE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(FOWLER, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 756l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(MARTINDALE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(FOWLER, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 757l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(CORAL_SPGS, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(FOWLER, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 758l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(TARADALE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(FOWLER, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 759l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(FALCONRIDGE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(FOWLER, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 760l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BONAVISTA_WEST, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(SCARLETT, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 761l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(SCARLETT, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(AUBURN_BAY, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 762l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BONAVISTA, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(SCARLETT, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 763l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(SCARLETT, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(WOODBINE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 764l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(SCARLETT, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(SOMERSET_BRIDLEWOOD_STATION, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 765l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(SCARLETT, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(SOMERSET_BRIDLEWOOD_STATION, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 766l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(SCARLETT, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(EVERGREEN, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 770l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(WESTERN_CANADA, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(OGDEN, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 771l) {
			if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CHINOOK_STATION, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 773l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(R_THIRSK, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(ROCKY_RIDGE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 774l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(R_THIRSK, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(ROYAL_OAK, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 775l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(CITADEL, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(R_THIRSK, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 776l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(WISE_WOOD, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(PALLISER_OAKRIDGE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 777l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(WISE_WOOD, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(EVERGREEN, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 778l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(WISE_WOOD, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(WOODLANDS, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 779l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(WISE_WOOD, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(WOODBINE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 780l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(WISE_WOOD, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(OAKRIDGE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 791l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(MAC_EWAN, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(QUEEN_ELIZABETH, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 792l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(SANDSTONE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(QUEEN_ELIZABETH, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 795l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(VINCENT_MASSEY, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(STRATHCONA, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 796l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(EDGEMONT, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(TOM_BAINES, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 798l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(TARADALE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(ANNIE_GALE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 799l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(CORAL_SPGS, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(ANNIE_GALE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 801l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BREBEUF, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(ROYAL_OAK, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 802l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BREBEUF, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(HAWKWOOD, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 804l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(SHERWOOD, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(BREBEUF, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 805l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(HAMPTONS, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(BREBEUF, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 807l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BREBEUF, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(ROCKY_RIDGE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 811l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(ST_FRANCIS, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(TUSCANY, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 812l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(ST_FRANCIS, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CITADEL, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 813l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(ST_FRANCIS, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(ARBOUR_LK, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 814l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(ST_FRANCIS, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(ROYAL_OAK, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 815l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(ST_FRANCIS, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(ARBOUR_LK, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 816l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(ST_FRANCIS, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CITADEL, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 817l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(ST_FRANCIS, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(ROCKY_RIDGE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 818l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(HAMPTONS, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(ST_FRANCIS, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 819l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(SHERWOOD, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(ST_FRANCIS, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 821l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(MTN_PARK, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(BISHOP_O_BYRNE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 822l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(MC_KENZIE_LK_WAY, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(BISHOP_O_BYRNE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 830l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(SANDSTONE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(M_D_HOUET, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 831l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(ST_FRANCIS, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(SCENIC_ACRES_NORTH, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 832l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(ST_FRANCIS, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(SCENIC_ACRES_SOUTH, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 833l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(DALHOUSIE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(M_D_HOUET, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 834l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(DALHOUSIE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(M_D_HOUET, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 835l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(ANDERSON, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 837l) {
			if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(SCENIC_ACRES_SOUTH, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 838l) {
			if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(SCENIC_ACRES_NORTH, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 841l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(NOTRE_DAME, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(HIDDEN_VLY, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 842l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(NOTRE_DAME, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(MAC_EWAN, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 851l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(LYNNWOOD, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(ST_AUGUSTINE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 853l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(RIVERBEND, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(ST_AUGUSTINE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 857l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(ST_STEPHEN, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(EVERGREEN, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 860l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(B_GRANDIN, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CRANSTON, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 861l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(B_GRANDIN, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(AUBURN_BAY, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 878l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(F_WHELIHAN, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CHAPARRAL, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 880l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(ST_MATTHEW, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(HERITAGE_STATION, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 883l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(EVANSTON, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 884l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(KINCORA, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 888l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(NORTH_POINTE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(ST_MARGARET, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 889l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BEDDINGTON, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.getId() == 892l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(ST_ISABELLA, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(MC_KENZIE, gTrip.getDirectionId());
				return;
			}
		}
		mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), gTrip.getDirectionId());
	}

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = tripHeadsign.toLowerCase(Locale.ENGLISH);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern ENDS_WITH_BOUND = Pattern.compile("([\\s]*[s|e|w|n]b[\\s]$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_BOUND = Pattern.compile("(^[\\s]*[s|e|w|n]b[\\s]*)", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_SLASH = Pattern.compile("(^[\\s]*/[\\s]*)", Pattern.CASE_INSENSITIVE);

	private static final String REGEX_START_END = "((^|[^A-Z]){1}(%s)([^a-zA-Z]|$){1})";
	private static final String REGEX_START_END_REPLACEMENT = "$2 %s $4";

	private static final Pattern AT_SIGN = Pattern.compile("([\\s]*@[\\s]*)", Pattern.CASE_INSENSITIVE);
	private static final String AT_SIGN_REPLACEMENT = " / ";

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
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = STARTS_WITH_SLASH.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		return CleanUtils.cleanLabel(gStopName);
	}

}
