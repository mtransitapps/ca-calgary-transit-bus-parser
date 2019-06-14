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
import org.mtransit.parser.mt.data.MInboundType;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

// https://www.calgarytransit.com/developer-resources
// https://data.calgary.ca/
// https://data.calgary.ca/en/Transportation-Transit/Calgary-Transit-Scheduling-Data/npk7-z3bj
// https://data.calgary.ca/download/npk7-z3bj/application%2Fzip
// https://data.calgary.ca/Transportation-Transit/Calgary-Transit-Scheduling-Data/npk7-z3bj
// https://data.calgary.ca/download/npk7-z3bj/application%2Fzip
// https://data.calgary.ca/d/npk7-z3bj?category=Transportation-Transit&view_name=Calgary-Transit-Scheduling-Data
// https://data.calgary.ca/api/file_data/38ff3c2d-efde-4d50-b83c-3a2f49f390e5?filename=CT_GTFS.zip
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

	public boolean isGoodEnoughAcceptedForSchoolsRoutes(long routeId) {
		if (routeId >= 732L && routeId <= 899L) {
			return true; // TODO clean school trip splitting
		}
		return false;
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		System.out.printf("\nGenerating Calgary Transit bus data...");
		long start = System.currentTimeMillis();
		boolean isNext = "next_".equalsIgnoreCase(args[2]);
		if (isNext) {
			setupNext();
		}
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("\nGenerating Calgary Transit bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	private void setupNext() {
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIds != null && this.serviceIds.isEmpty();
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

	private static final Pattern OUT_OF_SERVICE = Pattern.compile("((^|\\W){1}(out of service)(\\W|$){1})", Pattern.CASE_INSENSITIVE);

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (OUT_OF_SERVICE.matcher(gTrip.getTripHeadsign()).find()) {
			return true; // exclude
		}
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public boolean excludeRoute(GRoute gRoute) {
		return super.excludeRoute(gRoute);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	private static final String RSN_FLOATER = "FLT";
	private static final long RID_FLOATER = 10_001L;

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

	@SuppressWarnings("unused")
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
		if (rsn >= 600 && rsn <= 899) {
			return COLOR_BUS_ROUTES_SCHOOL;
		}
		if (ENDS_WITH_EXPRESS.matcher(gRoute.getRouteLongName()).find()) {
			return COLOR_BUS_ROUTES_EXPRESS;
		}
		if (STARTS_WITH_BRT.matcher(gRoute.getRouteLongName()).find()) {
			return COLOR_BUS_ROUTES_BRT;
		}
		if (rsn >= 1 && rsn <= 299) {
			return null;
		}
		if (rsn >= 400 && rsn <= 599) {
			return null;
		}
		if (isGoodEnoughAccepted()) {
			return null;
		}
		System.out.printf("\nUnexpected route color %s!\n", gRoute);
		System.exit(-1);
		return null;
	}

	private static final String SLASH = " / ";
	private static final String SPACE = " ";
	private static final String DASH = "-";

	private static final String _17_AVE_SE = "17 Ave Se";
	private static final String _69_ST_STATION = "69 St Sta";
	private static final String _69_ST_SW = "69 St SW";
	private static final String _78_AVE_TERMINAL = "78 Ave Terminal";
	private static final String AIRPORT = "Airport";
	private static final String ANDERSON = "Anderson";
	private static final String ANDERSON_STATION = ANDERSON + " Sta";
	private static final String ANNIE_GALE = "Annie Gale";
	private static final String APPLEWOOD = "Applewood";
	private static final String BEAVERBROOK = "Beaverbrook";
	private static final String BISHOP_O_BYRNE = "B O'Byrne";
	private static final String BOWNESS = "Bowness";
	private static final String BREBEUF = "Brebeuf";
	private static final String BRENTWOOD = "Brentwood";
	private static final String BRENTWOOD_STATION = BRENTWOOD + " Sta";
	private static final String BRIDLEWOOD = "Bridlewood";
	private static final String CANYON_MEADOWS = "Canyon Mdws";
	private static final String CANADA_OLYMPIC_PARK = "Canada Olympic Pk";
	private static final String CENTRAL_MEMORIAL = "Central Memorial";
	private static final String CHATEAU_EST = "Chateau Est";
	private static final String CHATEAU_ESTS = "Chateau Ests";
	private static final String CHINOOK = "Chinook";
	private static final String CHINOOK_STATION = CHINOOK + " Sta";
	private static final String CHURCHILL = "Churchill";
	private static final String CITY_CTR = "City Ctr";
	private static final String CITY_HALL = "City Hall";
	private static final String COACH_HILL = "Coach Hl";
	private static final String COPPERFIELD = "Copperfield";
	private static final String CORAL_SPGS = "Coral Spgs";
	private static final String COUGAR_RDG = "Cougar Rdg";
	private static final String COUNTRY_VLG = "Country Vlg";
	private static final String CRANSTON = "Cranston";
	private static final String CRESCENT_HTS = "Crescent Hts";
	private static final String CROWFOOT = "Crowfoot";
	private static final String DALHOUSIE = "Dalhousie";
	private static final String DEER_RUN = "Deer Run";
	private static final String DEERFOOT_CENTER = "Deerfoot Ctr";
	private static final String DIEFENBAKER = "Diefenbaker";
	private static final String DISCOVERY_RIDGE = "Discovery Rdg";
	private static final String DOUGLASDALE = "Douglasdale";
	private static final String DOUGLAS_GLEN = "Douglas Glen";
	private static final String EAST_HILLS = "East Hls";
	private static final String ERIN_WOODS = "Erin Woods";
	private static final String ERINWOODS = "Erinwoods";
	private static final String EVANSTON = "Evanston";
	private static final String EVERGREEN = "Evergreen";
	private static final String FOOTHILLS = "Foothills";
	private static final String FOOTHILLS_IND = FOOTHILLS + " Ind";
	private static final String FOREST_LAWN = "Forest Lawn";
	private static final String GLAMORGAN = "Glamorgan";
	private static final String HAMPTONS = "Hamptons";
	private static final String HERITAGE = "Heritage";
	private static final String HERITAGE_STATION = HERITAGE + " Sta";
	private static final String HUNTINGTON = "Huntington";
	private static final String KILLARNEY = "Killarney";
	private static final String KILLARNEY_17_AVE = KILLARNEY + " 17 Ave";
	private static final String KILLARNEY_26_AVE = KILLARNEY + " 26 Ave";
	private static final String LAKEVIEW = "Lakeview";
	private static final String MAC_EWAN = "MacEwan";
	private static final String MARLBOROUGH = "Marlborough";
	private static final String MARLBOROUGH_STATION = MARLBOROUGH + " Sta";
	private static final String MCCALL_WAY = "Mccall Way";
	private static final String MC_KENZIE = "McKenzie";
	private static final String MC_KENZIE_TOWNE = MC_KENZIE + " Towne";
	private static final String MC_KNIGHT = "McKnight";
	private static final String MC_KNIGHT_WESTWINDS = MC_KNIGHT + "-Westwinds";
	private static final String MCKNIGHT = "Mcknight";
	private static final String MCKNIGHT_WESTWINDS = MCKNIGHT + " Westwinds";
	private static final String MRU = "MRU";
	private static final String MOUNT_PLEASANT = "Mt Pleasant";
	private static final String MOUNT_MC_KENZIE = "Mt " + MC_KENZIE;
	private static final String NEW_BRIGHTON = "New Brighton";
	private static final String NOLAN_HILL = "Nolan Hl";
	private static final String NORTH = "North";
	private static final String NORTH_HAVEN = NORTH + " Haven";
	private static final String NORTH_POINTE = NORTH + " Pte";
	private static final String NORTHMOUNT_DR = "Northmount Dr";
	private static final String NOTRE_DAME = "Notre Dame";
	private static final String PANORAMA = "Panorama";
	private static final String PANORAMA_HLS = PANORAMA + " Hls";
	private static final String PANORAMA_HLS_NORTH = "N " + PANORAMA_HLS;
	private static final String PARKHILL = "Parkhill";
	private static final String PARKLAND = "Parkland";
	private static final String PARK_GATE_HERITAGE = "Pk Gt Heritage";
	private static final String QUEENSLAND = "Queensland";
	private static final String RAMSAY = "Ramsay";
	private static final String RENFREW = "Renfrew";
	private static final String RIVERBEND = "Riverbend";
	private static final String ROYAL_OAK = "Royal Oak";
	private static final String RUNDLE_STATION = "Rundle Sta";
	private static final String SADDLETOWNE = "Saddletowne";
	private static final String SAGE_HILL = "Sage Hl";
	private static final String SANDSTONE = "Sandstone";
	private static final String SCARLETT = "Scarlett";
	private static final String SCENIC_ACRES = "Scenic Acres";
	private static final String SCENIC_ACRES_NORTH = "N " + SCENIC_ACRES;
	private static final String SHERWOOD = "Sherwood";
	private static final String SILVER_SPGS = "Silver Spgs";
	private static final String SOMERSET = "Somerset";
	private static final String SOMERSET_STATION = SOMERSET + " Sta";
	private static final String SOUTH = "South";
	private static final String SOUTH_CALGARY = SOUTH + " Calgary";
	private static final String SOUTHCENTRE = "Southcentre";
	private static final String SOUTH_HEALTH = SOUTH + " Health";
	private static final String SOUTH_HOSPITAL = SOUTH + " Hosp";
	private static final String SOUTH_HEALTH_CAMPUS = SOUTH_HEALTH + " Campus";
	private static final String SOUTHLAND = "Southland";
	private static final String SOUTHLAND_STATION = SOUTHLAND + " Sta";
	private static final String STAMPEDE_PARK = "Stampede Pk";
	private static final String ST_FRANCIS = "St Francis";
	private static final String ST_MARGARET = "St Margaret";
	private static final String STATION_HERITAGE = "Sta " + HERITAGE;
	private static final String STRATHCONA = "Strathcona";
	private static final String TARADALE = "Taradale";
	private static final String TOM_BAINES = "Tom Baines";
	private static final String TUSCANY = "Tuscany";
	private static final String UNIVERSITY_OF_CALGARY = "University Of Calgary";
	private static final String VALLEY_RIDGE = "Vly Rdg";
	private static final String VISTA_HTS = "Vista Hts";
	private static final String WCHS_ST_MARY_S = "WCHS" + SLASH + "St Mary''s";
	private static final String WESTBROOK = "Westbrook";
	private static final String WESTBROOK_STATION = WESTBROOK + " Sta";
	private static final String WESTHILLS = "Westhills";
	private static final String WOODBINE = "Woodbine";

	private static HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;
	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<Long, RouteTripSpec>();
		map2.put(2L, new RouteTripSpec(2L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, MOUNT_PLEASANT, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, KILLARNEY_17_AVE) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"8068", // SB Glenside DR @ 17 AV SW
								"5144", // NB 8 ST SW @ 7 AV SW
								"5084", // EB 5 AV SW @ 2 ST SW
								"5579", // NB Centre ST N @ 78 AV N
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"5579", // NB Centre ST N @ 78 AV N
								"8531", // SB Centre ST N @ Beddington BV N
								"5038", // WB 78 AV N @ Centre ST N
								"5115", // WB 6 AV SW @ 1 ST SW
								"5122", // SB 8 ST SW @ 7 AV SW
								"8068", // SB Glenside DR @ 17 AV SW
						})) //
				.compileBothTripSort());
		map2.put(4L, new RouteTripSpec(4L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, HUNTINGTON, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CITY_CTR) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"6133", // WB 4 AV SW @ 7 ST SW
								"7433", // ++
								"5266", // EB @ 78 AV NW Terminal
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"5266", // EB @ 78 AV NW Terminal
								"7466", // WB 6 AV SE @ 1 ST SE
								"5115", // WB 6 AV SW @ 1 ST SW
								"6133", // WB 4 AV SW @ 7 ST SW
						})) //
				.compileBothTripSort());
		map2.put(5L, new RouteTripSpec(5L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, NORTH_HAVEN, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CITY_CTR) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"5252", // EB 5 AV SE @ Macleod TR
								"7295", // ++
								"9066", // SB Centre ST N @ 78 AV N
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"9066", // SB Centre ST N @ 78 AV N
								"3944", // EB 5 AV SW @ 9 ST SW
								"6537", // EB 5 AV SW @ 1 ST SW
								"5252", // EB 5 AV SE @ Macleod TR
						})) //
				.compileBothTripSort());
		map2.put(6L, new RouteTripSpec(6L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, CITY_CTR, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, KILLARNEY_26_AVE) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"3711", "8771", "5303" //
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"5303", // ==
								"5117", // !=
								"5118", // !=
								"5120", // ==
								"5122", "7941", "3711" //
						})) //
				.compileBothTripSort());
		map2.put(13L, new RouteTripSpec(13L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, CITY_CTR, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, WESTHILLS) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"6866", // SB Stewart GR @ S. of Richmond RD SW
								"3797", // Mount Royal University Terminal EB
								"5144", // NB 8 ST SW @ 7 AV SW
								"7337" // SB 1 ST SW @ 8 AV SW
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"7337", // SB 1 ST SW @ 8 AV SW
								"3798",// WB MRU West Gate
								"6866" // SB Stewart GR @ S. of Richmond RD SW
						})) //
				.compileBothTripSort());
		map2.put(14L, new RouteTripSpec(14L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, CRANSTON, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, BRIDLEWOOD) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"3621", // Somerset-Bridlewood Stn
								"6787", // ==
								"2458", // !=
								"6788", // !=
								"9773", // ==
								"7256", // WB Sun Valley BV @ Midpark BV SW
								"8508", // SB Shawville BV @ 162 AV SE
								"8668", // WB Shawville GA @ Somervale CO SW
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"8668", // WB Shawville GA @ Somervale CO SW
								"4820", // SB Bridleridge WY @ Bridleglen MR SW
								"3621", // Somerset-Bridlewood Stn
						})) //
				.compileBothTripSort());
		map2.put(15L, new RouteTripSpec(15L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.NORTH.getId(), // Fish Crk-Lacombe Sta
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.SOUTH.getId()) // SHAWVILLE
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "8668", "4795", "9224", "9189" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "9189", "7256", "8509", "8668" })) //
				.compileBothTripSort());
		map2.put(19L, new RouteTripSpec(19L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.EAST.getId(), // Rundle LRT Sta
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.WEST.getId()) // WB University WY @ Craigie Hall
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"5408", // != WB University WY @ Craigie Hall
								"6626", // != Lions Park LRT Station (EB 14 AV NW)
								"5703", // == EB 16 AV N @ Centre ST N
								"5706", // ++ 8 Ave NE @ 9 St NE
								"5710", // ++ 19 St NE @ Milne Dr
								"5725", // == Vista Heights Terminal
								"5718", // != Rundle LRT Station (SB 36 ST NE @ 25 AV NE)
								"7570", // != Rundle LRT Station (SB 36 ST NE @ 25 AV NE)
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"5718", // != Rundle LRT Station (SB 36 ST NE @ 25 AV NE)
								"7570", // != Rundle LRT Station (SB 36 ST NE @ 25 AV NE)
								"5728", // == WB 8 AV NE @ 19 ST NE
								"9413", // == 8 Ave NE @ Regal Cr
								"5734", // == SAIT Station (WB)
								"6626", // != Lions Pk LRT Station (EB 14 AV NW) =>
								"5735", // != Lions Pk LRT Sta (WB 14 Ave NW)
								"7573", // ++ University Dr @ 24 Ave NW
								"5408", // WB University WY @ Craigie Hall =>
						})) //
				.compileBothTripSort());
		map2.put(20L, new RouteTripSpec(20L, //
				0, MTrip.HEADSIGN_TYPE_STRING, NORTHMOUNT_DR, //
				1, MTrip.HEADSIGN_TYPE_STRING, HERITAGE) //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"5762", // Heritage LRT Station SB
								"6278", // NB Mount Royal CI @ MRU East Gate
								"5697", // WB University WY @ Craigie Hall
								"7626", // EB @ 78 AV N Terminal
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"7626", // EB @ 78 AV N Terminal
								"9009", // WB 4 ST W @ Centre ST N
								"5742", // WB Northmount DR @ 14 ST NW
								"7410", // WB University WY @ Craigie Hall
								"5762", // Heritage LRT Station SB
						})) //
				.compileBothTripSort());
		map2.put(23L, new RouteTripSpec(23L, //
				0, MTrip.HEADSIGN_TYPE_STRING, SADDLETOWNE, //
				1, MTrip.HEADSIGN_TYPE_STRING, MC_KENZIE_TOWNE) //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"3610", // SB 52 St @ Mckenzie Towne Li SE
								"5063", // NB 52 ST SE @ 17 AV SE
								"6384", // NB 52 ST NE @ Rundlehorn DR
								"7762", // NB Falconridge BV @ Falworth RD NE
								"8576", // Saddletowne LRT Station SB
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"8576", // Saddletowne LRT Station SB
								"4947", // SB 52 ST SE @ 17 AV SE
								"3610", // SB 52 St @ Mckenzie Towne Li SE
						})) //
				.compileBothTripSort());
		map2.put(26L, new RouteTripSpec(26L, //
				MInboundType.INBOUND.intValue(), MTrip.HEADSIGN_TYPE_INBOUND, MInboundType.INBOUND.getId(), //
				MInboundType.OUTBOUND.intValue(), MTrip.HEADSIGN_TYPE_INBOUND, MInboundType.OUTBOUND.getId()) //
				.addTripSort(MInboundType.INBOUND.intValue(), //
						Arrays.asList(new String[] { "5872", "5878", "5885", "6709" })) //
				.addTripSort(MInboundType.OUTBOUND.intValue(), //
						Arrays.asList(new String[] { "6709", "4472", "5898", "8141", "8140", "5872" })) //
				.compileBothTripSort());
		map2.put(30L, new RouteTripSpec(30L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.EAST.getId(), //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.WEST.getId()) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"7792", // EB 7 AV SW @ 2 ST SW
								"5574", // EB 39 AV SE @ Burnsland RD
								"5981", // ++
								"7320", // SB 12 ST SE @ 42 AV SE
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"7320", // SB 12 ST SE @ 42 AV SE
								"4131", // ++
								"5574", // EB 39 AV SE @ Burnsland RD
						})) //
				.compileBothTripSort());
		map2.put(37L, new RouteTripSpec(37L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, HERITAGE, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CANYON_MEADOWS) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"3415", // SB 6 ST SW @ Canyon Meadows DR SW
								"5194", // != Heritage LRT Station
								"3756", // != Heritage LRT Station
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"5194", // != Heritage LRT Station
								"3756", // != Heritage LRT Station
								"3411", // EB Canterbury Dr @ Canyon Meadows Dr SW
								"3415", // SB 6 ST SW @ Canyon Meadows DR SW
						})) //
				.compileBothTripSort());
		map2.put(52L, new RouteTripSpec(52L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, EVERGREEN, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, SOMERSET) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "3622", "9207", "4994", "7997", "6863", "9189" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "9189", "4798", "9199", "9207", "3622" })) //
				.compileBothTripSort());
		map2.put(56L, new RouteTripSpec(56L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, SOUTHLAND_STATION, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, ANDERSON_STATION) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "6461", "6097" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "6097", "6562", "6461" })) //
				.compileBothTripSort());
		map2.put(66L, new RouteTripSpec(66L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, SADDLETOWNE, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CHINOOK) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"6342", // Chinook LRT Station WB
								"8576", // Saddletowne LRT Station SB
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"8576", // Saddletowne LRT Station SB
								"6342", // Chinook LRT Station WB
						})) //
				.compileBothTripSort());
		map2.put(68L, new RouteTripSpec(68L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.NORTH.getId(), // Saddletowne LRT Station SB
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.SOUTH.getId()) // East Hills
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"2298", // NB East Hills SQ @ East Hills BV SE
								"4124", // ==
								"8585", // != Saddletowne LRT Station SB =>
								"2728", // != Saddletowne LRT Station SB =>
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"2728", // != Saddletowne LRT Station SB <=
								"8585", // != Saddletowne LRT Station SB <=
								"9440", // ==
								"2298", // NB East Hills SQ @ East Hills BV SE
						})) //
				.compileBothTripSort());
		map2.put(69L, new RouteTripSpec(69L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, DEERFOOT_CENTER, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CITY_CTR) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"5053", // EB 7 AV SW @ 2 ST SW
								"6680", // NB 9 ST NE @ 64 AV NE
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"6680", // NB 9 ST NE @ 64 AV NE
								"7334", // WB 65 AV NE @ 9 ST NE
								"5001", // WB 7 AV SW @ 2 ST SW
						})) //
				.compileBothTripSort());
		map2.put(71L, new RouteTripSpec(71L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, SADDLETOWNE, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, MC_KNIGHT_WESTWINDS) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"9848", // McKnight-Westwinds LRT Station
								"8580", // Saddletowne LRT Station SB
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"8580", // Saddletowne LRT Station SB
								"9848", // McKnight-Westwinds LRT Station
						})) //
				.compileBothTripSort());
		map2.put(72L, new RouteTripSpec(72L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, BRENTWOOD, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CHINOOK) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "6342", "5684", "6348", "6748" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "6748", "6339", "5779", "9036", "6096", "6358", "6301", "6362", "5853", "5267", "6342" })) //
				.compileBothTripSort());
		map2.put(73L, new RouteTripSpec(73L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, BRENTWOOD, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CHINOOK) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "6374", "8118", "8144", "6592", "5742", "8979", "3845" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "3845", "6369", "6374" })) //
				.compileBothTripSort());
		map2.put(75L, new RouteTripSpec(75L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CITY_CTR, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, RIVERBEND) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"8058", // SB 18 ST SE @ N. of Riverbend GA
								"8064", // NB 18 ST SE @ Riverglen DR
								"9978", // WB 6 AV SW @ 8 ST SW
								"2377", // WB 6 AV SW @ 9 ST SW
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"3944", // EB 5 AV SW @ 9 ST SW
								"8058", // SB 18 ST SE @ N. of Riverbend GA
								"8064" // NB 18 ST SE @ Riverglen DR
						})) //
				.compileBothTripSort());
		map2.put(79L, new RouteTripSpec(79L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, HERITAGE_STATION, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, SOUTHLAND_STATION) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "6420", "6423", "4244", "6412" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "6412", "6416", "6420" })) //
				.compileBothTripSort());
		map2.put(80L, new RouteTripSpec(80L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, HERITAGE_STATION, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, SOUTHLAND_STATION) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "6440", "6116", "5762" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "5762", "6433", "6440" })) //
				.compileBothTripSort());
		map2.put(81L, new RouteTripSpec(81L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.NORTH.getId(), //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.SOUTH.getId()) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"9324", // SB Lake Fraser DR @ N. End of Avenida PL SE
								"2497", // Anderson LRT Station
								"5528", // Chinook LRT Station NB
								"6914", // WB 50 AV SW @ 4 ST SW
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"6914", // WB 50 AV SW @ 4 ST SW
								"7024", // Chinook LRT Station WB
								"3650", // Canyon Meadows Stn (NB Lake Fraser Dr)
								"9324", // SB Lake Fraser DR @ N. End of Avenida PL SE
						})) //
				.compileBothTripSort());
		map2.put(85L, new RouteTripSpec(85L, //
				0, MTrip.HEADSIGN_TYPE_STRING, SADDLETOWNE, //
				1, MTrip.HEADSIGN_TYPE_STRING, MCKNIGHT_WESTWINDS) //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"9645", // McKnight-Westwinds LRT Station
								"4983", // ++
								"8597", // Saddletowne LRT Station NB
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"2728", // Saddletowne LRT Station SB
								"9638", // ++
								"9645", // McKnight-Westwinds LRT Station
						})) //
				.compileBothTripSort());
		map2.put(86L, new RouteTripSpec(86L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.NORTH.getId(), //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.SOUTH.getId()) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"9767", // NB Harvest Hills BV @ Country Village WY NE
								"8468", // SB Country Village LI @ Country Village WY NE
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"8468", // SB Country Village LI @ Country Village WY NE
								"9767", // NB Harvest Hills BV @ Country Village WY NE
						})) //
				.compileBothTripSort());
		map2.put(94L, new RouteTripSpec(94L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, STRATHCONA, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, WESTBROOK_STATION) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { "3741", "5315", "8379", "6515" })) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { "6515", "3732", "7597", "3741" })) //
				.compileBothTripSort());
		map2.put(100L, new RouteTripSpec(100L, //
				0, MTrip.HEADSIGN_TYPE_STRING, NORTH_POINTE, //
				1, MTrip.HEADSIGN_TYPE_STRING, MCKNIGHT_WESTWINDS) //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"9644", // McKnight-Westwinds LRT Station
								"4100", // SB Country Village LI @ Country Village WY NE
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"4100", // SB Country Village LI @ Country Village WY NE
								"9644", // McKnight-Westwinds LRT Station
						})) //
				.compileBothTripSort());
		map2.put(112L, new RouteTripSpec(112L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, CITY_CTR, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, WESTHILLS) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { "6866", "5432", "8650", "5299" })) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { "5299", "8200", "6524", "9080", //
								"6540", "6541", //
								"6545", //
								"6542", "6866" })) //
				.compileBothTripSort());
		map2.put(120L, new RouteTripSpec(120L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CROWFOOT, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, DALHOUSIE) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"4025", // Dalhousie LRT Station NB
								"3857" // Crowfoot LRT Station
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"3857", // Crowfoot LRT Station
								"4025", // Dalhousie LRT Station NB
						})) //
				.compileBothTripSort());
		map2.put(125L, new RouteTripSpec(125L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, ERIN_WOODS, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, CITY_CTR) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"3944", // EB 5 AV SW @ 9 ST SW
								"6303", // EB Erin Woods BV @ 36 ST SE
								"6305", // SB Erin Woods BV @ Erin Dale CR SE
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"6303", // EB Erin Woods BV @ 36 ST SE
								"6305", // SB Erin Woods BV @ Erin Dale CR SE
								"9978", // WB 6 AV SW @ 8 ST SW
								"2377", // WB 6 AV SW @ 9 ST SW
						})) //
				.compileBothTripSort());
		map2.put(126L, new RouteTripSpec(126L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, APPLEWOOD, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, CITY_CTR) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"3944", // EB 5 AV SW @ 9 ST SW
								"8823", // NB 68 ST SE @ 14 AV SE
								"6934", // WB Applewood DR @ Appletree CR SE
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"8823", // NB 68 ST SE @ 14 AV SE
								"6934", // WB Applewood DR @ Appletree CR SE
								"9978", // WB 6 AV SW @ 8 ST SW
								"2377", // WB 6 AV SW @ 9 ST SW
						})) //
				.compileBothTripSort());
		map2.put(134L, new RouteTripSpec(134L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CROWFOOT, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, DALHOUSIE) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"4025", // Dalhousie LRT Station NB
								"3857" // Crowfoot LRT Station
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"3857", // Crowfoot LRT Station
								"4025", // Dalhousie LRT Station NB
						})) //
				.compileBothTripSort());
		map2.put(158L, new RouteTripSpec(158L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, ROYAL_OAK, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, TUSCANY) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"2150", // Tuscany Station - Rocky Ridge Terminal
								"3533", // ++
								"9575", // == NB Rocky Ridge RD @ Royal Oak GD NW
								"3535", // != EB 112 AV NW @ Rocky Ridge RD
								"3451", // !=
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"3451", // !=
								"3535", // != EB 112 AV NW @ Rocky Ridge RD
								"9574", // == SB Country Hills BV @ Royal Oak Centre NW
								"8892", // ++
								"2150", // Tuscany Station - Rocky Ridge Terminal
						})) //
				.compileBothTripSort());
		map2.put(164L, new RouteTripSpec(164L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Aspen Summit", //
				1, MTrip.HEADSIGN_TYPE_STRING, _69_ST_STATION) //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"8373", "8398", // 69 Street West LRT Station
								"2439", // ++
								"2442", // WB 14 AV SW @ Aspen Ridge WY
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"2442", // WB 14 AV SW @ Aspen Ridge WY
								"2445", // ++
								"9687", // ==
								"8373", "8398", // 69 Street West LRT Station
						})) //
				.compileBothTripSort());
		map2.put(167L, new RouteTripSpec(167L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Walden", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Legacy") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"2210", // NB Legacy VW SE @ Legacy MR SE
								"5809", // ++
								"2248", // EB Shawville WY @ W. of Shawville LI SE
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"2248", // EB Shawville WY @ W. of Shawville LI SE
								"2202", // ++
								"2210", // NB Legacy VW SE @ Legacy MR SE
						})) //
				.compileBothTripSort());
		map2.put(168L, new RouteTripSpec(168L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Walden", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Legacy") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"2229", // SB Legacy VW SE @ Legacy Legacy CI SE
								"2240", // ++
								"2248", // EB Shawville WY @ W. of Shawville LI SE
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"2248", // EB Shawville WY @ W. of Shawville LI SE
								"4779", // ++
								"2229", // SB Legacy VW SE @ Legacy Legacy CI SE
						})) //
				.compileBothTripSort());
		map2.put(174L, new RouteTripSpec(174L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.NORTH.getId(), //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.SOUTH.getId()) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"4127", // WB Tuscany Ravine RD @ Tuscany Ravine HT NW
								"3823", // ++
								"2153", // Tuscany Station - Tuscany Terminal
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"2153", // Tuscany Station - Tuscany Terminal
								"3833", // ++
								"4127", // WB Tuscany Ravine RD @ Tuscany Ravine HT NW
						})) //
				.compileBothTripSort());
		map2.put(176L, new RouteTripSpec(176L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, SADDLETOWNE, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, SOUTH) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"3902", // <> WB 114 AV SE (WBay) @ 29 ST SE
								"7635", // <>
								"4980", // <>
								"4979", // <> SB 114 AV SE @ 29 ST SE
								"9488", // !=
								"8576", // Saddletowne LRT Station SB
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"8576", // Saddletowne LRT Station SB
								"9487", // !=
								"3902", // <> WB 114 AV SE (WBay) @ 29 ST SE
								"7635", // <>
								"4980", // <>
								"4979", // <> SB 114 AV SE @ 29 ST SE
						})) //
				.compileBothTripSort());
		map2.put(300L, new RouteTripSpec(300L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, AIRPORT, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CITY_CTR) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"3881", // WB 4 AV SW @ 2 ST SW
								"3884", // EB 9 AV S @ Centre ST S
								"3880", // WB 4 AV SE @ 1 ST SE
								"3900", // YYC Airport Domestic Terminal
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"3900", // YYC Airport Domestic Terminal
								"3881", // WB 4 AV SW @ 2 ST SW
						})) //
				.compileBothTripSort());
		map2.put(301L, new RouteTripSpec(301L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, NORTH, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CITY_CTR) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"2030", // SB 7 ST SW @ 7 AVE SW
								"4267", // ++
								"4456", // NB Harvest Hills BV @ Country Village WY NE
								"4593", // SB Country Village LI @ Country Village WY NE
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"4456", // NB Harvest Hills BV @ Country Village WY NE
								"4593", // SB Country Village LI @ Country Village WY NE
								"4592", // ++
								"2030", // SB 7 ST SW @ 7 AVE SW
						})) //
				.compileBothTripSort());
		map2.put(302L, new RouteTripSpec(302L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CITY_CTR, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, SOUTH_HEALTH) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"2200", // WB Front ST @ Main Ent. South Health Campus SE
								"3876", // ==
								"3803", // !=
								"3613", // !=
								"9820", // ==
								"9823", // WB 6 AV SE @ Macleod TR
								"3882", // SB 5 ST SW @ 8 AV SW
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"3882", // SB 5 ST SW @ 8 AV SW
								"3884", // EB 9 AV S @ Centre ST S
								"2200", // WB Front ST @ Main Ent. South Health Campus SE
						})) //
				.compileBothTripSort());
		map2.put(404L, new RouteTripSpec(404L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.EAST.getId(), //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.WEST.getId()) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { "6628", "6730" })) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { "6730", "6628" })) //
				.compileBothTripSort());
		map2.put(408L, new RouteTripSpec(408L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, BRENTWOOD, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, VALLEY_RIDGE) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"7706", // Valley Ridge East Terminal
								"9136", // WB Crestmont BV @ Cresthaven PL SW
								"6752", // Brentwood LRT Station
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"6752", // Brentwood LRT Station
								"9136", // WB Crestmont BV @ Cresthaven PL SW
								"7706", // Valley Ridge East Terminal
						})) //
				.compileBothTripSort());
		map2.put(410L, new RouteTripSpec(410L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CHINOOK_STATION, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, HERITAGE_STATION) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"6412", // Heritage LRT Station NB <= START
								"4938", // !=
								"9964", // != SB Blackfoot TR @ 71 AV SE <= START
								"7729", // ==
								"5552", // ==
								"6374", // Chinook LRT Station EB => END
								"7024", // Chinook LRT Station WB => END
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"6374", // != Chinook LRT Station EB <= START
								"7024", // != Chinook LRT Station WB <= START
								"5529", // ==
								"7277", // ==
								"9964", // != SB Blackfoot TR @ 71 AV SE => END
								"7727", // !=
								"6412", // Heritage LRT Station NB => END
						})) //
				.compileBothTripSort());
		map2.put(414L, new RouteTripSpec(414L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, NORTH_HAVEN, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CITY_CTR) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"5331", // SB 14 ST SW@ 17 AV SW
								"5288", // NB 14 ST SW@ 17 AV SW
								"7437", // SB Niven RD @ Norquay DR NW
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"7437", // SB Niven RD @ Norquay DR NW
								"5331" // SB 14 ST SW@ 17 AV SW
						})) //
				.compileBothTripSort());
		map2.put(419L, new RouteTripSpec(419L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, FOOTHILLS, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, PARKHILL) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "5574", "5299", "5227", "8339", "8340", })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "8340", "8339", "5108", "5580", "5574" })) //
				.compileBothTripSort());
		map2.put(425L, new RouteTripSpec(425L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, COUNTRY_VLG, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, SAGE_HILL) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { "4734", "4442", "3497" })) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { "3497", "4450", "4734" })) //
				.compileBothTripSort());
		map2.put(430L, new RouteTripSpec(430L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, NORTH_POINTE, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, SANDSTONE) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"9873", // Sandstone Terminal
								"4318", // ++
								"3497", // WB Country Village WY @ Harvest Hills BV N
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"3497", // WB Country Village WY @ Harvest Hills BV N
								"4317", // ++
								"9873", // Sandstone Terminal
						})) //
				.compileBothTripSort());
		map2.put(439L, new RouteTripSpec(439L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, _69_ST_STATION, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, DISCOVERY_RIDGE) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"9365", // EB Discovery Ridge BV @ Discovery Ridge LN SW
								"3785", // NB 69 ST SW @ 69 Street West LRT Station
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"3785", // NB 69 ST SW @ 69 Street West LRT Station
								"9366", // WB Discovery Ridge BV @ Discovery Ridge GA SW
								"9365", // EB Discovery Ridge BV @ Discovery Ridge LN SW
						})) //
				.compileBothTripSort());
		map2.put(502L, new RouteTripSpec(502L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, STATION_HERITAGE, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, PARK_GATE_HERITAGE) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { "7592", "5192", "5762" })) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { "5762", "4577", "7592" })) //
				.compileBothTripSort());
		map2.put(695L, new RouteTripSpec(695L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, ST_FRANCIS, // BOWNESS
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, VALLEY_RIDGE) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"9136", // WB Crestmont BV @ Cresthaven PL SW
								"8840" // EB Northmont DR @ Calandar RD NW
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						// no stops
						})) //
				.compileBothTripSort());
		map2.put(698L, new RouteTripSpec(698L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, WCHS_ST_MARY_S, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, _69_ST_STATION) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"3785", // NB 69 ST SW @ 69 Street West LRT Station
								"5324", // EB 17 AV SW@ 2 ST SW
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"5287", // WB 17 AV SW@ 1 ST SW
								"3785", // NB 69 ST SW @ 69 Street West LRT Station
						})) //
				.compileBothTripSort());
		map2.put(699L, new RouteTripSpec(699L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Schools", // Central Memorial
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "West Spgs") // West Spgs
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"8822", // NB 77 ST SW @ Old Banff Coach RD
								"4690", // EB 50 AV SW @ 21A ST SW
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"4791", // WB 50 AV SW @ 22 ST SW
								"8822", // NB 77 ST SW @ Old Banff Coach RD
								"4924", // EB Old Banff Coach RD @ 73 St SW
						})) //
				.compileBothTripSort());
		map2.put(702L, new RouteTripSpec(702L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, NOLAN_HILL, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CHURCHILL) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"5598", // NB Northland DR @ 52 AV NW
								"2039", // WB Nolan Hill Blvd @ Shaganappi Tr NW
								"2048", // ==
								"2049", // !=
								"2050", // !=
								"2051", // ==
								"2034" // EB Nolan Hill Blvd farside Nolan Hill Dr
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"2034", // EB Nolan Hill Blvd farside Nolan Hill Dr
								"5491" // SB Northland DR @ 52 AV NW
						})) //
				.compileBothTripSort());
		map2.put(703L, new RouteTripSpec(703L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, SHERWOOD, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CHURCHILL) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"5598", // NB Northland DR @ 52 AV NW
								"8176", // SB Sherwood BV @ Sherwood ST NW
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"8176", // SB Sherwood BV @ Sherwood ST NW
								"5598", // NB Northland DR @ 52 AV NW
						})) //
				.compileBothTripSort());
		map2.put(704L, new RouteTripSpec(704L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Edgepark Blvd", // HAMPTONS // COUNTRY_HLS
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CHURCHILL) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"5598", // NB Northland DR @ 52 AV NW
								"7707", // Hamptons Bus Terminal WB
								"8544", // EB Country Hills BV @ Edgebrook BV NW
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"8640", // WB Country Hills BV @ Hamptons DR NW
								"7707", // Hamptons Bus Terminal WB
								"5491", // SB Northland DR @ 52 AV NW
						})) //
				.compileBothTripSort());
		map2.put(705L, new RouteTripSpec(705L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Edgepark Rise", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CHURCHILL) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"5598", // NB Northland DR @ 52 AV NW
								"6600", // Edgebrook RI NW Terminal
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"6600", // Edgebrook RI NW Terminal
								"5491", // SB Northland DR @ 52 AV NW
						})) //
				.compileBothTripSort());
		map2.put(706L, new RouteTripSpec(706L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, HAMPTONS, // "Edenwold Dr", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CHURCHILL) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"5598", // NB Northland DR @ 52 AV NW
								"7707", // Hamptons Bus Terminal WB
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"7707", // Hamptons Bus Terminal WB
								"8357", // SB Edgebrook BV @ Country Hills BV NW
								"5491", // SB Northland DR @ 52 AV NW
						})) //
				.compileBothTripSort());
		map2.put(710L, new RouteTripSpec(710L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, BEAVERBROOK, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CRANSTON) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"4794", // <> SB Cranston BV @ Cranleigh DR SE
								"9291", // <> NB Cranston DR @ Cranleigh GA SE
								"8443", // !=
								"7736", // ==
								"5615", // !=
								"6585", // !=
								"7034", // !=
								"4210", // == WB 90 AV SE @ Fairmount DR
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"4210", // == WB 90 AV SE @ Fairmount DR
								"6510", // !=
								"7683", // !=
								"6970", // ==
								"4163", // !=
								"4794", // <> SB Cranston BV @ Cranleigh DR SE
								"9291", // <> NB Cranston DR @ Cranleigh GA SE
						})) //
				.compileBothTripSort());
		map2.put(711L, new RouteTripSpec(711L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, DOUGLAS_GLEN, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, BEAVERBROOK) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"4210", // WB 90 AV SE @ Fairmount DR
								"7377", // SB Mount McKenzie DR @ 130 AV SE
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"8318", // NB Douglasdale BV @ 130 AV SE
								"4210", // WB 90 AV SE @ Fairmount DR
						})) //
				.compileBothTripSort());
		map2.put(712L, new RouteTripSpec(712L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, BEAVERBROOK, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, PARKLAND) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"6468", // SB Parkvalley DR @ W. of Parkridge DR SE
								"4210", // WB 90 AV SE @ Fairmount DR
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"4826", // SB Fairmount DR @ 88 AV SE
								"6468", // SB Parkvalley DR @ W. of Parkridge DR SE
						})) //
				.compileBothTripSort());
		map2.put(713L, new RouteTripSpec(713L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, BEAVERBROOK, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, DEER_RUN) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"6270", // NB 146 AV SE @ Deer Lake RD Terminal
								"8297", // NB Deer Run BV @ Deer Lane CL SE
								"6273", // ++
								"4210", // WB 90 AV SE @ Fairmount DR
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"4826", // SB Fairmount DR @ 88 AV SE
								"8302", // ++
								"6270", // NB 146 AV SE @ Deer Lake RD Terminal
								"8297", // NB Deer Run BV @ Deer Lane CL SE
						})) //
				.compileBothTripSort());
		map2.put(714L, new RouteTripSpec(714L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, BEAVERBROOK, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, MC_KENZIE_TOWNE) // PRESTWICK
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"2302", // NB 52 ST SE @ Copperfield GA
								"4904", // NB Prestwick BV @ Prestwick CI SE
								"4210", // WB 90 AV SE @ Fairmount DR
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"4826", // SB Fairmount DR @ 88 AV SE
								"8327", // SB Prestwick BV @ Prestwick CI SE
								"2303", // SB 52 ST SE @ Copperfield GA
						})) //
				.compileBothTripSort());
		map2.put(715L, new RouteTripSpec(715L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, BEAVERBROOK, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, QUEENSLAND) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"8315", // EB Queensland PL @ Queensland DR SE
								"8316", // WB Queensland PL @ Queensland DR SE
								"8317", // ++
								"4210", // WB 90 AV SE @ Fairmount DR
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"4826", // SB Fairmount DR @ 88 AV SE
								"9163", // ++
								"8315", // EB Queensland PL @ Queensland DR SE
								"8316", // WB Queensland PL @ Queensland DR SE
						})) //
				.compileBothTripSort());
		map2.put(716L, new RouteTripSpec(716L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, NEW_BRIGHTON, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, BEAVERBROOK) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"4826", // SB Fairmount DR @ 88 AV SE
								"3897", // ++
								"7798", // EB New Brighton DR @ 52 ST SE
								"9864", // WB New Brighton DR @ W. of New Brighton DR SE

						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"7798", // EB New Brighton DR @ 52 ST SE
								"9864", // WB New Brighton DR @ W. of New Brighton DR SE
								"3868", // ++
								"4210", // WB 90 AV SE @ Fairmount DR
						})) //
				.compileBothTripSort());
		map2.put(717L, new RouteTripSpec(717L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, COPPERFIELD, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, BEAVERBROOK) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { "4826", "9621", "3512" })) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { "3511", "3969", "4210" })) //
				.compileBothTripSort());
		map2.put(718L, new RouteTripSpec(718L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, DOUGLASDALE, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, BEAVERBROOK) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"4210", // WB 90 AV SE @ Fairmount DR
								"7036", // ++
								"6596", // Douglasbank WY Terminal SE
								"6512", // WB Douglasdale BV @ Douglas Woods WY SE
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"6596", // Douglasbank WY Terminal SE
								"6512", // WB Douglasdale BV @ Douglas Woods WY SE
								"7034", // ++
								"4210", // WB 90 AV SE @ Fairmount DR
						})) //
				.compileBothTripSort());
		map2.put(719L, new RouteTripSpec(719L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, MC_KENZIE, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, BEAVERBROOK) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"4210", // == WB 90 AV SE @ Fairmount DR
								"6510", // !=
								"8319", // !=
								"7377", // ==
								"6970", // WB McKenzie Lake BV @ Mount McKenzie DR SE
								"7051", // !=
								"7039", // <> EB McKenzie DR @ McKenzie Lake BV SE
								"6585", // <> EB McKenzie Lake BV @ McKenzie Lake WY SE
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"7039", // <> EB McKenzie DR @ McKenzie Lake BV SE
								"6585", // <> EB McKenzie Lake BV @ McKenzie Lake WY SE
								"7682", // !=
								"7376", // ==
								"8318", // !=
								"4899", // !=
								"4896", // !=
								"4210", // == WB 90 AV SE @ Fairmount DR
						})) //
				.compileBothTripSort());
		map2.put(720L, new RouteTripSpec(720L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, BEAVERBROOK, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, MOUNT_MC_KENZIE) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"7051", // EB Mountain Park DR @ Mount Kidd RD SE
								"4896", // EB 130 AV SE @ Mount McKenzie DR
								"4210" // WB 90 AV SE @ Fairmount DR
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"4210", // WB 90 AV SE @ Fairmount DR
								"6970", // WB McKenzie Lake BV @ Mount McKenzie DR SE
								"7051" // EB Mountain Park DR @ Mount Kidd RD SE
						})) //
				.compileBothTripSort());
		map2.put(721L, new RouteTripSpec(721L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, BOWNESS, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, VALLEY_RIDGE) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						/* no stops */
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"4063", // WB 46 AV NW @ 77 ST NW
								"7473", // ++
								"4942", // EB Valley Ridge DR @ Valley Crest CL NW
						})) //
				.compileBothTripSort());
		map2.put(722L, new RouteTripSpec(722L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, BOWNESS, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, TUSCANY) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"4127", // WB Tuscany Ravine RD @ Tuscany Ravine HT NW
								"4060" // SB 77 ST NW @ 46 AV NW
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"4060", // SB 77 ST NW @ 46 AV NW
								"4127" // WB Tuscany Ravine RD @ Tuscany Ravine HT NW
						})) //
				.compileBothTripSort());
		map2.put(723L, new RouteTripSpec(723L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, BOWNESS, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, TUSCANY) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"9594", // <> SB Tuscany DR @ Toscana GD NW
								"4718", // <> EB Tuscany Estates DR @ Tuscany Glen PA NW
								"6875", // !=
								"4060", // SB 77 ST NW @ 46 AV NW
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"4060", // SB 77 ST NW @ 46 AV NW
								"3834", // !=
								"9594", // <> SB Tuscany DR @ Toscana GD NW
								"4718", // <> EB Tuscany Estates DR @ Tuscany Glen PA NW
						})) //
				.compileBothTripSort());
		map2.put(724L, new RouteTripSpec(724L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, TUSCANY, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, BOWNESS) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"4060", // SB 77 ST NW @ 46 AV NW
								"4061", // ++
								"6731", // ++
								"8432", // NB Tuscany Springs BV @ Tuscany BV NW
								"3822", // SB Tuscany WY @ Tuscany Ridge HT NW
								"8003", // EB Tuscany BV @ Tuscany Hills RD NW
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"8432", // NB Tuscany Springs BV @ Tuscany BV NW
								"3822", // SB Tuscany WY @ Tuscany Ridge HT NW
								"8003", // EB Tuscany BV @ Tuscany Hills RD NW
								"5179", // ++
								"6317", // ++
								"4060", // SB 77 ST NW @ 46 AV NW
						})) //
				.compileBothTripSort());
		map2.put(725L, new RouteTripSpec(725L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, SILVER_SPGS, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, BOWNESS) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"4060", // SB 77 ST NW @ 46 AV NW
								"5824", //
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"5811", //
								"4060", // SB 77 ST NW @ 46 AV NW
						})) //
				.compileBothTripSort());
		map2.put(731L, new RouteTripSpec(731L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, RIVERBEND, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, CENTRAL_MEMORIAL) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"4083", // SB 21 ST SW @ 51 AV SW
								"2470", // SB 18 St @ Rivervalley Dr SE
								"8058", // ++
								"6909", // SB 18 ST SE @ Riverview CL
								"8064", // NB 18 ST SE @ Riverglen DR
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"2470", // SB 18 St @ Rivervalley Dr SE
								"6909", // SB 18 ST SE @ Riverview CL
								"8064", // NB 18 ST SE @ Riverglen DR
								"4690", // ++
								"4083", // SB 21 ST SW @ 51 AV SW
						})) //
				.compileBothTripSort());
		map2.put(732L, new RouteTripSpec(732L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, CENTRAL_MEMORIAL, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, GLAMORGAN) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"6867", // NB Sierra Morena BV @ Signal Hill CTR SW
								"9640", // EB Discovery Ridge BV @ Discovery Ridge CI SW
								"4083", // SB 21 ST SW @ 51 AV SW
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"4083", // SB 21 ST SW @ 51 AV SW
								"6867", // NB Sierra Morena BV @ Signal Hill CTR SW
								"9640", // EB Discovery Ridge BV @ Discovery Ridge CI SW
						})) //
				.compileBothTripSort());
		map2.put(738L, new RouteTripSpec(738L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, PANORAMA_HLS_NORTH, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, DIEFENBAKER) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"5783", // NB 4 ST NW @ 68 AV NW (JG Diefenbaker HS)
								"4114", // WB Panatela GA @ Harvest Hills BV NE
								"4414", // WB Panatella BV @ Panatella ST NW
								"9703", // SB Panamount BV @ S. of Panamount PZ NW
								"4304", // ==
								"4095", // !=
								"4094", // !=
								"4093", // ==
								"4100", // SB Country Village LI @ Country Village WY NE
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"4100", // SB Country Village LI @ Country Village WY NE
								"8421", // ++
								"5783", // NB 4 ST NW @ 68 AV NW (JG Diefenbaker HS)
						})) //
				.compileBothTripSort());
		map2.put(748L, new RouteTripSpec(748L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Hidden Ranch", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CRESCENT_HTS) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"2025", // != Crescent Heights High School <=
								"112113", // != 12 AV NW @ 1 ST NW - Crescent Heights <=
								"7100", // ==
								"9705", // EB Hidden Creek WY @ Hidden Creek PT NW
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"9344", // WB Hidden Creek WY @ Hidden Creek PT NW
								"9347", // ==
								"9756", // !=
								"7701", // ==
								"112112", // Crescent Heights High School
						})) //
				.compileBothTripSort());
		map2.put(750L, new RouteTripSpec(750L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Nelson Mandela", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CORAL_SPGS) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "9625", "5792" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "7671", "9440" })) //
				.compileBothTripSort());
		map2.put(767L, new RouteTripSpec(767L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.NORTH.getId(), // ANDERSON_STATION
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.SOUTH.getId()) // SCARLETT
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"9904", // WB Canterbury DR @ Canterbury PL SW
								"7013", // ==
								"5221", // !=
								"7020", // !=
								"5206", // ==
								"5974", // Anderson LRT Station
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"5974", // Anderson LRT Station
								"9904", // WB Canterbury DR @ Canterbury PL SW
								"7013", // ==
								"5205", // !=
								"8689", // !=
								"5206", // ==
								"5974", // Anderson LRT Station
						})) //
				.compileBothTripSort());
		map2.put(768L, new RouteTripSpec(768L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, SCARLETT, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Walden") // Legacy
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"3570", // SB Walden BV @ N. leg Walden DR SE midblk
								"9904", // WB Canterbury DR @ Canterbury PL SW
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"9904", // WB Canterbury DR @ Canterbury PL SW
								"3574", // NB Walden BV @ nleg Walden Dr SE midblk
						})) //
				.compileBothTripSort());
		map2.put(782L, new RouteTripSpec(782L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, NOLAN_HILL, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "HD Cartwrightl") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"2427", // SB 53 Street @ Dalhart Rd nearside
								"2034", // EB Nolan Hill BV @ Nolan Hill DR farside
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"2039", // WB Nolan Hill BV @ Shaganappi TR NW
								"2427", // SB 53 Street @ Dalhart Rd nearside
						})) //
				.compileBothTripSort());
		map2.put(797L, new RouteTripSpec(797L, //
				0, MTrip.HEADSIGN_TYPE_STRING, TOM_BAINES, // AM
				1, MTrip.HEADSIGN_TYPE_STRING, HAMPTONS) // PM
				.addTripSort(0, //
						Arrays.asList(new String[] { "7707", "6399", "4014", "4013" })) //
				.addTripSort(1, //
						Arrays.asList(new String[] { "4013", "8537", "7707", "6399" })) //
				.compileBothTripSort());
		map2.put(798L, new RouteTripSpec(798L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, TARADALE, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, ANNIE_GALE) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "4898", "6572", "9533" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "9290", "4723", "4898" })) //
				.compileBothTripSort());
		map2.put(801L, new RouteTripSpec(801L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, BREBEUF, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, ROYAL_OAK) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { "8367", "9598", "6862" })) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { "6862", "6851", "8367", "9598" })) //
				.compileBothTripSort());
		map2.put(820L, new RouteTripSpec(820L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, ST_FRANCIS, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, SCENIC_ACRES_NORTH) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"7159", // NB Scurfield DR @ Scripps LD NW
								"8840", // EB Northmont DR @ Calandar RD NW
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"8976", // WB Northmount DR @ Clarendon RD NW
								"5222", // EB Scenic Acres BV @ Scenic Acres DR NW
						})) //
				.compileBothTripSort());
		map2.put(822L, new RouteTripSpec(822L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Walden", // Legacy
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, BISHOP_O_BYRNE) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"2164", // EB Shawville GA @ Shawville LI SE
								"3574", // NB Walden BV @ nleg Walden Dr SE midblk
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"3570", // SB Walden BV @ nleg Walden Dr SE midblk
								"2164", // EB Shawville GA @ Shawville LI SE
						})) //
				.compileBothTripSort());
		map2.put(836L, new RouteTripSpec(836L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, SCENIC_ACRES, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "St V De Paul") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"7092", // EB Varsity DR @ 49 ST NW
								"7164", // SB Scurfield DR @ Scenic GD NW
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"6535", // NB Scurfield DR @ Scenic Acres BV NW
								"7092", // EB Varsity DR @ 49 ST NW
						})) //
				.compileBothTripSort());
		map2.put(843L, new RouteTripSpec(843L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, NOTRE_DAME, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, MAC_EWAN) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"7364", // WB Berkley DR @ Bermondsey RI NW
								"2018", // NB Country Village LI south of Country Village Rd
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"4085", // WB Country Village WY @ Country Village LINK NE
								"7332", // EB Berkley DR @ Hunterview DR NW
						})) //
				.compileBothTripSort());
		map2.put(844L, new RouteTripSpec(844L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, NOTRE_DAME, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, EVANSTON) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						/* no stops */
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"4085", // WB Country Village WY @ Country Village LINK NE
								"3700", // WB Symons Valley PY @ Evanspark BV NW
						})) //
				.compileBothTripSort());
		map2.put(862L, new RouteTripSpec(862L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Bishop Grandin", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "52 St") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"2302", // NB 52 ST SE @ Copperfield GA
								"8879", // SB 52 ST SE @ 130 AV SE
								"4727", // EB 86 AV SW @ Haddon RD
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"2727", // SB Haddon Rd @ Hogarth Rd
								"3617", // NB 52 ST SE @ New Brighton DR
								"2303", // SB 52 ST SE @ Copperfield GA
						})) //
				.compileBothTripSort());
		map2.put(894L, new RouteTripSpec(894L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, _69_ST_SW, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, STRATHCONA) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"9284", // NB Strathcona DR @ Strathlea AV SW
								"6515", // EB Strathcona DR @ Strathcona BV SW
								"4167", // WB Strathcona DR @ Strathcona DR SW
								"3409", // EB 26 Ave @ 51 St SW
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"3408", // WB 26 Ave @ St. Gregory School
								"6515", // EB Strathcona DR @ Strathcona BV SW
								"4167", // WB Strathcona DR @ Strathcona DR SW
								"9283", // SB Strathcona DR @ Strathlea CR SW
						})) //
				.compileBothTripSort());
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public int compareEarly(long routeId, List<MTripStop> list1, List<MTripStop> list2, MTripStop ts1, MTripStop ts2, GStop ts1GStop, GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop, this);
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
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()), this);
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return; // split
		}
		if (isGoodEnoughAcceptedForSchoolsRoutes(mRoute.getId())) { // School routes
			int directionId = gTrip.getDirectionId() == null ? 0 : gTrip.getDirectionId();
			mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()) + (directionId == 0 ? "" : " "), directionId);
			return;
		}
		mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), gTrip.getDirectionId() == null ? 0 : gTrip.getDirectionId());
	}

	@Override
	public boolean mergeHeadsign(MTrip mTrip, MTrip mTripToMerge) {
		if (MTrip.mergeEmpty(mTrip, mTripToMerge)) {
			return true;
		}
		List<String> headsignsValues = Arrays.asList(mTrip.getHeadsignValue(), mTripToMerge.getHeadsignValue());
		if (mTrip.getRouteId() == 1L) {
			if (Arrays.asList( //
					CITY_CTR, //
					BOWNESS //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(BOWNESS, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					CITY_CTR, //
					FOREST_LAWN //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(FOREST_LAWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 3L) {
			if (Arrays.asList( //
					CITY_CTR, //
					HERITAGE_STATION //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(HERITAGE_STATION, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 7L) {
			if (Arrays.asList( //
					SOUTH_CALGARY, //
					CITY_CTR //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CITY_CTR, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 8L) {
			if (Arrays.asList( //
					UNIVERSITY_OF_CALGARY, //
					NORTH_POINTE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(NORTH_POINTE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 9L) {
			if (Arrays.asList( //
					CHINOOK, //
					CITY_HALL //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CITY_HALL, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					UNIVERSITY_OF_CALGARY, //
					CHINOOK //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CHINOOK, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 10L) {
			if (Arrays.asList( //
					CITY_CTR, //
					CHINOOK, //
					CITY_HALL //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CITY_HALL, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					CITY_CTR, //
					SOUTHCENTRE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SOUTHCENTRE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 14L) {
			if (Arrays.asList( //
					BRIDLEWOOD, //
					CRANSTON //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CRANSTON, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					SOMERSET + DASH + BRIDLEWOOD, //
					CRANSTON + SLASH + SOMERSET_STATION //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CRANSTON + SLASH + SOMERSET_STATION, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 17L) {
			if (Arrays.asList( //
					CITY_CTR, //
					RAMSAY //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(RAMSAY, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					CITY_CTR, //
					RENFREW //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(RENFREW, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 18L) {
			if (Arrays.asList( //
					MRU, //
					CITY_CTR //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CITY_CTR, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					MRU, //
					LAKEVIEW //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(LAKEVIEW, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 20L) {
			if (Arrays.asList( //
					_78_AVE_TERMINAL, //
					BRENTWOOD_STATION, //
					UNIVERSITY_OF_CALGARY, //
					HERITAGE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(HERITAGE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 21L) {
			if (Arrays.asList( //
					MARLBOROUGH_STATION, //
					FOOTHILLS_IND //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(FOOTHILLS_IND, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 33L) {
			if (Arrays.asList( //
					VISTA_HTS, //
					RUNDLE_STATION //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(RUNDLE_STATION, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 38L) {
			if (Arrays.asList( //
					"Whitehorn Sta", //
					"Temple" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Temple", mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"Whitehorn Sta", //
					"Brentwood" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Brentwood", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 57L) {
			if (Arrays.asList( //
					MARLBOROUGH_STATION, //
					ERINWOODS //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ERINWOODS, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					MARLBOROUGH_STATION, //
					MCCALL_WAY // MC_CALL_WAY //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(MCCALL_WAY, mTrip.getHeadsignId()); // MC_CALL_WAY
				return true;
			}
			if (Arrays.asList( //
					"Whitehorn Sta", //
					MCCALL_WAY //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(MCCALL_WAY, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"Whitehorn Sta", //
					"Monterey Pk" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Monterey Pk", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 82L) {
			if (Arrays.asList( //
					NOLAN_HILL, //
					UNIVERSITY_OF_CALGARY //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(NOLAN_HILL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 93L) {
			if (Arrays.asList( //
					_69_ST_STATION, //
					COACH_HILL //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(COACH_HILL, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					_69_ST_STATION, //
					WESTBROOK //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(WESTBROOK, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 96L) {
			if (Arrays.asList( //
					MC_KENZIE, // <>
					WOODBINE, //
					ANDERSON_STATION //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ANDERSON_STATION, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 107L) {
			if (Arrays.asList( //
					SOUTH_CALGARY, //
					CITY_CTR //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CITY_CTR, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 127L) {
			if (Arrays.asList( //
					MARLBOROUGH, //
					"Franklin Industrial" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Franklin Industrial", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 135L) {
			if (Arrays.asList( //
					"36 St Se" + SLASH + ERIN_WOODS, // <>
					MARLBOROUGH //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(MARLBOROUGH, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 302L) {
			if (Arrays.asList( //
					CITY_CTR, //
					SOUTH_HEALTH_CAMPUS //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SOUTH_HEALTH_CAMPUS, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 304L) {
			if (Arrays.asList( //
					"Out Of Service Next Bus Please", //
					STAMPEDE_PARK //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(STAMPEDE_PARK, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 305L) {
			if (Arrays.asList( //
					CITY_CTR, //
					_17_AVE_SE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(_17_AVE_SE, mTrip.getHeadsignId()); // 17 Ave SE
				return true;
			} else if (Arrays.asList( //
					CITY_CTR, //
					CANADA_OLYMPIC_PARK //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CANADA_OLYMPIC_PARK, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 406L) {
			if (Arrays.asList( //
					"Auburn Bay", //
					SOMERSET + "-" + BRIDLEWOOD + " Sta" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SOMERSET + "-" + BRIDLEWOOD + " Sta", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 421L) {
			if (Arrays.asList( //
					NORTH_POINTE, //
					"Panatella" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Panatella", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 440L) {
			if (Arrays.asList( //
					EAST_HILLS + SLASH + CHATEAU_EST, //
					EAST_HILLS, //
					CHATEAU_EST //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CHATEAU_ESTS, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 468L) {
			if (Arrays.asList( //
					SOUTH_HOSPITAL, //
					CRANSTON //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CRANSTON, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 627L) {
			if (Arrays.asList( //
					"School Charter", //
					"Columbia College" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Columbia College", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 765L) {
			if (Arrays.asList( //
					"Ep Scarlett Woodbine", //
					"Ep Scarlett Somerset-Bridlewood Sta" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Ep Scarlett Woodbine", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 810L) {
			if (Arrays.asList( //
					ST_MARGARET + SPACE + ST_FRANCIS + SLASH + NORTH_POINTE, //
					ST_MARGARET + SPACE + NORTH_POINTE //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ST_MARGARET + SPACE + NORTH_POINTE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == RID_FLOATER) {
			if (Arrays.asList( //
					"Out Of Service Next Bus Please", //
					"Stampede Parade" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Stampede Parade", mTrip.getHeadsignId());
				return true;
			}
		}
		System.out.printf("\nUnexpected trips to merge %s & %s!\n", mTrip, mTripToMerge);
		System.exit(-1);
		return false;
	}

	private static final Pattern AVENUE_ = Pattern.compile("((^|\\W){1}(av)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String AVENUE_REPLACEMENT = "$2Avenue$4";

	private static final Pattern MRU_ = Pattern.compile("((^|\\W){1}(mru|mount royal university)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String MRU_REPLACEMENT = "$2" + MRU + "$4";

	private static final Pattern MC_KENZIE_ = Pattern.compile("((^|\\W){1}(mckenzie)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String MC_KENZIE_REPLACEMENT = "$2" + MC_KENZIE + "$4";

	private static final Pattern STN = Pattern.compile("((^|\\W){1}(stn)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String STN_REPLACEMENT = "$2Station$4";

	private static final Pattern ENDS_WITH_EXPRESS = Pattern.compile("((\\W){1}(express)($){1})", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_BRT = Pattern.compile("((^){1}(brt)(\\W){1})", Pattern.CASE_INSENSITIVE);

	private static final Pattern ROUTE_RSN = Pattern.compile("((^){1}(route )?([\\d]+)($){1})", Pattern.CASE_INSENSITIVE);

	private static final Pattern ENDS_WITH_VIA = Pattern.compile("( via .*$)", Pattern.CASE_INSENSITIVE);

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		if (Utils.isUppercaseOnly(tripHeadsign, true, true)) {
			tripHeadsign = tripHeadsign.toLowerCase(Locale.ENGLISH);
		}
		tripHeadsign = AVENUE_.matcher(tripHeadsign).replaceAll(AVENUE_REPLACEMENT);
		tripHeadsign = STARTS_WITH_BRT.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = MRU_.matcher(tripHeadsign).replaceAll(MRU_REPLACEMENT);
		tripHeadsign = MC_KENZIE_.matcher(tripHeadsign).replaceAll(MC_KENZIE_REPLACEMENT);
		tripHeadsign = STN.matcher(tripHeadsign).replaceAll(STN_REPLACEMENT);
		tripHeadsign = ENDS_WITH_EXPRESS.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = ROUTE_RSN.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = ENDS_WITH_VIA.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = CleanUtils.cleanSlashes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern ENDS_WITH_BOUND = Pattern.compile("([\\s]*[s|e|w|n]b[\\s]$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_BOUND = Pattern.compile("(^[\\s]*[s|e|w|n]b[\\s]*)", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_SLASH = Pattern.compile("(^[\\s]*/[\\s]*)", Pattern.CASE_INSENSITIVE);

	private static final String REGEX_START_END = "((^|[^A-Z]){1}(%s)([^a-zA-Z]|$){1})";
	private static final String REGEX_START_END_REPLACEMENT = "$2 %s $4";

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
