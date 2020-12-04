package org.mtransit.parser.ca_calgary_transit_bus;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

// https://www.calgarytransit.com/developer-resources
// https://data.calgary.ca/
// https://data.calgary.ca/en/Transportation-Transit/Calgary-Transit-Scheduling-Data/npk7-z3bj
// https://data.calgary.ca/download/npk7-z3bj/application%2Fzip
// https://data.calgary.ca/Transportation-Transit/Calgary-Transit-Scheduling-Data/npk7-z3bj
// https://data.calgary.ca/download/npk7-z3bj/application%2Fzip
// https://data.calgary.ca/d/npk7-z3bj?category=Transportation-Transit&view_name=Calgary-Transit-Scheduling-Data
// https://data.calgary.ca/api/file_data/38ff3c2d-efde-4d50-b83c-3a2f49f390e5?filename=CT_GTFS.zip
public class CalgaryTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@Nullable String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-calgary-transit-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new CalgaryTransitBusAgencyTools().start(args);
	}

	private boolean isGoodEnoughAcceptedForSchoolsRoutes(long routeId) {
		return routeId >= 600L && routeId <= 899L; // TODO clean school trip splitting
	}

	@Nullable
	private HashSet<Integer> serviceIds;

	@Override
	public void start(@NotNull String[] args) {
		MTLog.log("Generating Calgary Transit bus data...");
		long start = System.currentTimeMillis();
		boolean isNext = "next_".equalsIgnoreCase(args[2]);
		if (isNext) {
			setupNext();
		}
		this.serviceIds = extractUsefulServiceIdInts(args, this, true);
		super.start(args);
		MTLog.log("Generating Calgary Transit bus data... DONE in %s.", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	private void setupNext() {
		// DO NOTHING
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIds != null && this.serviceIds.isEmpty();
	}

	@Override
	public boolean excludeCalendar(@NotNull GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarInt(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(@NotNull GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDateInt(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	private static final Pattern OUT_OF_SERVICE = Pattern.compile("((^|\\W)(out of service)(\\W|$))", Pattern.CASE_INSENSITIVE);

	@Override
	public boolean excludeTrip(@NotNull GTrip gTrip) {
		if (OUT_OF_SERVICE.matcher(gTrip.getTripHeadsignOrDefault()).find()) {
			return true; // exclude
		}
		if (this.serviceIds != null) {
			return excludeUselessTripInt(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public boolean excludeRoute(@NotNull GRoute gRoute) {
		return super.excludeRoute(gRoute);
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	private static final String RSN_FLOATER = "FLT";
	private static final long RID_FLOATER = 10_001L;

	@Override
	public long getRouteId(@NotNull GRoute gRoute) {
		if (!Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			if (RSN_FLOATER.equals(gRoute.getRouteShortName())) {
				return RID_FLOATER;
			}
		}
		return Long.parseLong(gRoute.getRouteShortName()); // using route short name as route ID
	}

	private static final Pattern CLEAN_STREET_POINT = Pattern.compile("((\\s)*(ave|st|mt)\\.(\\s)*)", Pattern.CASE_INSENSITIVE);
	private static final String CLEAN_AVE_POINT_REPLACEMENT = "$2$3$4";

	@NotNull
	@Override
	public String getRouteLongName(@NotNull GRoute gRoute) {
		String gRouteLongName = gRoute.getRouteLongName();
		gRouteLongName = CleanUtils.cleanSlashes(gRouteLongName);
		gRouteLongName = CLEAN_STREET_POINT.matcher(gRouteLongName).replaceAll(CLEAN_AVE_POINT_REPLACEMENT);
		gRouteLongName = CleanUtils.cleanStreetTypes(gRouteLongName);
		return CleanUtils.cleanLabel(gRouteLongName);
	}

	private static final String AGENCY_COLOR_RED = "B83A3F"; // LIGHT RED (from web site CSS)

	private static final String AGENCY_COLOR = AGENCY_COLOR_RED;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@SuppressWarnings("unused")
	private static final String COLOR_BUS_ROUTES = "004B85"; // BLUE (from PDF map)
	private static final String COLOR_BUS_ROUTES_EXPRESS = "00BBE5"; // LIGHT BLUE (from PDF map)
	private static final String COLOR_BUS_ROUTES_BRT = "ED1C2E"; // RED (from PDF map)
	private static final String COLOR_BUS_ROUTES_SCHOOL = "E4A024"; // YELLOW (from PDF map)

	@Nullable
	@Override
	public String getRouteColor(@NotNull GRoute gRoute) {
		final String rsnS = gRoute.getRouteShortName();
		if (!Utils.isDigitsOnly(rsnS)) {
			if (RSN_FLOATER.equals(rsnS)) {
				return null;
			}
		}
		int rsn = Integer.parseInt(rsnS);
		if (rsn >= 600 && rsn <= 899) {
			return COLOR_BUS_ROUTES_SCHOOL;
		}
		final String rln = gRoute.getRouteLongNameOrDefault();
		if (ENDS_WITH_EXPRESS.matcher(rln).find()) {
			return COLOR_BUS_ROUTES_EXPRESS;
		}
		if (STARTS_WITH_BRT.matcher(rln).find()) {
			return COLOR_BUS_ROUTES_BRT;
		}
		if (rsn >= 1 && rsn <= 299) {
			return null;
		}
		if (rsn == 303) { // MO MAX Orange
			return "EF8B22";
		} else if (rsn == 304) { // MY MAX Yellow
			return "FFCD02";
		} else if (rsn == 306) { // MT MAX Teal
			return "009bA7";
		} else if (rsn == 307) { // MP MAX Purple
			return "92368D";
		}
		if (rsn >= 400 && rsn <= 599) {
			return null;
		}
		throw new MTLog.Fatal("Unexpected route color %s!", gRoute);
	}

	private static final String _SLASH_ = " / ";
	private static final String SPACE = " ";
	private static final String DASH = "-";

	private static final String _17_AVE_SE = "17 Ave Se";
	private static final String _69_ST_STATION = "69 St Sta";
	private static final String AIRPORT = "Airport";
	private static final String ANDERSON = "Anderson";
	private static final String ANDERSON_STATION = ANDERSON + " Sta";
	private static final String BEAVERBROOK = "Beaverbrook";
	private static final String BOWNESS = "Bowness";
	private static final String BRENTWOOD = "Brentwood";
	private static final String BRENTWOOD_STATION = BRENTWOOD + " Sta";
	private static final String BRIDLEWOOD = "Bridlewood";
	private static final String BRIDLEWOOD_STATION = BRIDLEWOOD + " Sta";
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
	private static final String CRANSTON = "Cranston";
	private static final String CROWFOOT = "Crowfoot";
	private static final String DALHOUSIE = "Dalhousie";
	private static final String DEER_RUN = "Deer Run";
	private static final String DEERFOOT_CENTER = "Deerfoot Ctr";
	private static final String DIEFENBAKER = "Diefenbaker";
	private static final String DOUGLAS_GLEN = "Douglas Glen";
	private static final String EAST_HILLS = "East Hls";
	private static final String ERIN_WOODS = "Erin Woods";
	private static final String FOOTHILLS = "Foothills";
	private static final String FOOTHILLS_IND = FOOTHILLS + " Ind";
	private static final String FOREST_LAWN = "Forest Lawn";
	private static final String HAMPTONS = "Hamptons";
	private static final String HERITAGE = "Heritage";
	private static final String HERITAGE_STATION = HERITAGE + " Sta";
	private static final String HUNTINGTON = "Huntington";
	private static final String KILLARNEY = "Killarney";
	private static final String KILLARNEY_SLASH_17_AVE_SW = KILLARNEY + _SLASH_ + "17 Ave Sw";
	private static final String LAKEVIEW = "Lakeview";
	private static final String MARLBOROUGH = "Marlborough";
	private static final String MARLBOROUGH_STATION = MARLBOROUGH + " Sta";
	private static final String MCCALL_WAY_NE = "Mccall Wy Ne";
	private static final String MC_KENZIE = "McKenzie";
	private static final String MC_KENZIE_TOWNE = MC_KENZIE + " Towne";
	private static final String MC_KNIGHT = "McKnight";
	private static final String MC_KNIGHT_WESTWINDS = MC_KNIGHT + "-Westwinds";
	private static final String MCKNIGHT = "Mcknight";
	private static final String MCKNIGHT_WESTWINDS = MCKNIGHT + " Westwinds";
	private static final String MRU = "MRU";
	private static final String MOUNT_PLEASANT = "Mt Pleasant";
	private static final String MOUNT_ROYAL = "Mt Royal";
	private static final String NOLAN_HILL = "Nolan Hl";
	private static final String NORTH = "North";
	private static final String NORTH_HAVEN = NORTH + " Haven";
	private static final String NORTH_POINTE = NORTH + " Pte";
	private static final String NORTHMOUNT_DR = "Northmount Dr";
	private static final String NORTHMOUNT_DR_N = NORTHMOUNT_DR + " N";
	private static final String PANORAMA = "Panorama";
	private static final String PANORAMA_HLS = PANORAMA + " Hls";
	private static final String PANORAMA_HLS_NORTH = "N " + PANORAMA_HLS;
	private static final String PARKLAND = "Parkland";
	private static final String QUEENSLAND = "Queensland";
	private static final String RAMSAY = "Ramsay";
	private static final String RENFREW = "Renfrew";
	private static final String RIVERBEND = "Riverbend";
	private static final String RUNDLE_STATION = "Rundle Sta";
	private static final String SADDLETOWNE = "Saddletowne";
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
	private static final String STAMPEDE_PARK = "Stampede Pk";
	private static final String ST_FRANCIS = "St Francis";
	private static final String ST_MARGARET = "St Margaret";
	private static final String TUSCANY = "Tuscany";
	private static final String UNIVERSITY_OF_CALGARY = "University Of Calgary";
	private static final String VALLEY_RIDGE = "Vly Rdg";
	private static final String VISTA_HTS = "Vista Hts";
	private static final String WCHS_ST_MARY_S = "WCHS" + _SLASH_ + "St Mary''s";
	private static final String WESTBROOK = "Westbrook";
	private static final String WESTBROOK_STATION = WESTBROOK + " Sta";
	private static final String WESTHILLS = "Westhills";
	private static final String WHITEHORN_STATION = "Whitehorn Sta";
	private static final String WOODBINE = "Woodbine";

	private static final HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;

	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<>();
		//noinspection deprecation
		map2.put(4L, new RouteTripSpec(4L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, HUNTINGTON, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CITY_CTR) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"6133", // WB 4 AV SW @ 7 ST SW
								"7433", // ++
								"5266" // EB @ 78 AV NW Terminal
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"5266", // EB @ 78 AV NW Terminal
								"7466", // WB 6 AV SE @ 1 ST SE
								"5115", // WB 6 AV SW @ 1 ST SW
								"6133" // WB 4 AV SW @ 7 ST SW
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(5L, new RouteTripSpec(5L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, NORTH_HAVEN, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CITY_CTR) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"5252", // EB 5 AV SE @ Macleod TR
								"7295", // ++
								"9066" // SB Centre ST N @ 78 AV N
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"9066", // SB Centre ST N @ 78 AV N
								"3944", // EB 5 AV SW @ 9 ST SW
								"6537", // EB 5 AV SW @ 1 ST SW
								"5252" // EB 5 AV SE @ Macleod TR
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(19L, new RouteTripSpec(19L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.EAST.getId(), // Rundle LRT Sta
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.WEST.getId()) // WB University WY @ Craigie Hall
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList( //
								"5408", // != WB University WY @ Craigie Hall
								"6626", // != Lions Park LRT Station (EB 14 AV NW)
								"5703", // == EB 16 AV N @ Centre ST N
								"5706", // ++ 8 Ave NE @ 9 St NE
								"5710", // ++ 19 St NE @ Milne Dr
								"5725", // == Vista Heights Terminal
								"5718", // != Rundle LRT Station (SB 36 ST NE @ 25 AV NE)
								"7570" // != Rundle LRT Station (SB 36 ST NE @ 25 AV NE)
						)) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList( //
								"5718", // != Rundle LRT Station (SB 36 ST NE @ 25 AV NE)
								"7570", // != Rundle LRT Station (SB 36 ST NE @ 25 AV NE)
								"5712", // Vista Heights Terminal
								"5728", // == WB 8 AV NE @ 19 ST NE
								"9413", // == 8 Ave NE @ Regal Cr
								"5732", // Centre Street N Station (WB)
								"5734", // == SAIT Station (WB)
								"6626", // != Lions Pk LRT Station (EB 14 AV NW) =>
								"5735", // != Lions Pk LRT Sta (WB 14 Ave NW)
								"7573", // ++ University Dr @ 24 Ave NW
								"5408" // WB University WY @ Craigie Hall =>
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(23L, new RouteTripSpec(23L, //
				0, MTrip.HEADSIGN_TYPE_STRING, SADDLETOWNE, //
				1, MTrip.HEADSIGN_TYPE_STRING, MC_KENZIE_TOWNE) //
				.addTripSort(0, //
						Arrays.asList( //
								"3610", // SB 52 St @ Mckenzie Towne Li SE
								"5063", // NB 52 ST SE @ 17 AV SE
								"6384", // NB 52 ST NE @ Rundlehorn DR
								"7762", // NB Falconridge BV @ Falworth RD NE
								"8576" // Saddletowne LRT Station SB
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"8576", // Saddletowne LRT Station SB
								"8156", // SB 52 ST NE @ Rundlehorn DR
								"4947", // SB 52 ST SE @ 17 AV SE
								"3610" // SB 52 St @ Mckenzie Towne Li SE
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(30L, new RouteTripSpec(30L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.EAST.getId(), //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.WEST.getId()) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList( //
								"7792", // EB 7 AV SW @ 2 ST SW
								"5574", // EB 39 AV SE @ Burnsland RD
								"5981", // ++
								"7320" // SB 12 ST SE @ 42 AV SE
						)) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList( //
								"7320", // SB 12 ST SE @ 42 AV SE
								"4131", // ++
								"5574" // EB 39 AV SE @ Burnsland RD
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(56L, new RouteTripSpec(56L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, HERITAGE_STATION, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, ANDERSON_STATION) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"6461", // Anderson LRT Station
								"6101", // SB Woodpark BV @ Woodview DR SW
								"5762" // Heritage LRT Station SB
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"5762", // Heritage LRT Station SB
								"6562", // SB 24 ST SW @ Woodglen RI
								"6461" // Anderson LRT Station
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(68L, new RouteTripSpec(68L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.NORTH.getId(), // Saddletowne LRT Station SB
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.SOUTH.getId()) // East Hills
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"2298", // <> NB East Hills SQ @ East Hills BV SE <=
								"3742", // <> SB 84 ST SE @ 17 AV SE
								"2299", // <> WB 17th AV SE @ 84th ST SE <=
								"2180", // !=
								"4124", // ==
								"2728" // != Saddletowne LRT Station SB =>
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"2728", // != Saddletowne LRT Station SB <=
								"9440", // ==
								"2178", // == !=
								"2298", // != <> NB East Hills SQ @ East Hills BV SE =>
								"3743", // != <> NB 84 ST SE @ 17 AV SE
								"3742", // != <> SB 84 ST SE @ 17 AV SE
								"2299" // != <> WB 17th AV SE @ 84th ST SE =>
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(69L, new RouteTripSpec(69L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, DEERFOOT_CENTER, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CITY_CTR) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"5053", // EB 7 AV SW @ 2 ST SW
								"6680" // NB 9 ST NE @ 64 AV NE
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"6680", // NB 9 ST NE @ 64 AV NE
								"7334", // WB 65 AV NE @ 9 ST NE
								"5001" // WB 7 AV SW @ 2 ST SW
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(71L, new RouteTripSpec(71L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, SADDLETOWNE, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, MC_KNIGHT_WESTWINDS) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"9848", // McKnight-Westwinds LRT Station
								"8580" // Saddletowne LRT Station SB
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"8580", // Saddletowne LRT Station SB
								"9848" // McKnight-Westwinds LRT Station
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(85L, new RouteTripSpec(85L, //
				0, MTrip.HEADSIGN_TYPE_STRING, SADDLETOWNE, //
				1, MTrip.HEADSIGN_TYPE_STRING, MCKNIGHT_WESTWINDS) //
				.addTripSort(0, //
						Arrays.asList( //
								"9645", // McKnight-Westwinds LRT Station
								"4983", // ++
								"8597" // Saddletowne LRT Station NB
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"2728", // Saddletowne LRT Station SB
								"9638", // ++
								"9645" // McKnight-Westwinds LRT Station
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(86L, new RouteTripSpec(86L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.NORTH.getId(), //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.SOUTH.getId()) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"9767", // NB Harvest Hills BV @ Country Village WY NE
								"8468" // SB Country Village LI @ Country Village WY NE
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"8468", // SB Country Village LI @ Country Village WY NE
								"9767" // NB Harvest Hills BV @ Country Village WY NE
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(120L, new RouteTripSpec(120L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CROWFOOT, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, DALHOUSIE) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"4025", // Dalhousie LRT Station NB
								"3857" // Crowfoot LRT Station
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"3857", // Crowfoot LRT Station
								"4025" // Dalhousie LRT Station NB
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(134L, new RouteTripSpec(134L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CROWFOOT, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, DALHOUSIE) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"4025", // Dalhousie LRT Station NB
								"3857" // Crowfoot LRT Station
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"3857", // Crowfoot LRT Station
								"4025" //  Dalhousie LRT Station NB
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(174L, new RouteTripSpec(174L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.NORTH.getId(), //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.SOUTH.getId()) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"4127", // WB Tuscany Ravine RD @ Tuscany Ravine HT NW
								"3823", // ++
								"2153" //  Tuscany Station - Tuscany Terminal
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"2153", // Tuscany Station - Tuscany Terminal
								"3833", // ++
								"4127" //  WB Tuscany Ravine RD @ Tuscany Ravine HT NW
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(408L, new RouteTripSpec(408L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, BRENTWOOD, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, VALLEY_RIDGE) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList( //
								"7706", // Valley Ridge East Terminal
								"9136", // WB Crestmont BV @ Cresthaven PL SW
								"6752" //  Brentwood LRT Station
						)) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList( //
								"6752", // Brentwood LRT Station
								"9136", // WB Crestmont BV @ Cresthaven PL SW
								"7706" //  Valley Ridge East Terminal
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(410L, new RouteTripSpec(410L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CHINOOK_STATION, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, HERITAGE_STATION) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"6412", // Heritage LRT Station NB <= START
								"4938", // !=
								"9964", // != SB Blackfoot TR @ 71 AV SE <= START
								"7729", // ==
								"5552", // ==
								"6374", // Chinook LRT Station EB => END
								"7024" //  Chinook LRT Station WB => END
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"6374", // != Chinook LRT Station EB <= START
								"7024", // != Chinook LRT Station WB <= START
								"5529", // ==
								"7277", // ==
								"9964", // != SB Blackfoot TR @ 71 AV SE => END
								"7727", // !=
								"6412" //  Heritage LRT Station NB => END
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(414L, new RouteTripSpec(414L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, NORTH_HAVEN, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CITY_CTR) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"5331", // SB 14 ST SW@ 17 AV SW
								"5288", // NB 14 ST SW@ 17 AV SW
								"7437" //  SB Niven RD @ Norquay DR NW
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"7437", // SB Niven RD @ Norquay DR NW
								"5331" // SB 14 ST SW@ 17 AV SW
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(698L, new RouteTripSpec(698L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, WCHS_ST_MARY_S, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, _69_ST_STATION) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList( //
								"3785", // NB 69 ST SW @ 69 Street West LRT Station
								"3732", // EB 17 AV SW @ 69 Street West LRT Station
								"5324" //  EB 17 AV SW@ 2 ST SW
						)) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList( //
								"5287", // WB 17 AV SW@ 1 ST SW
								"3785" //  NB 69 ST SW @ 69 Street West LRT Station
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(699L, new RouteTripSpec(699L, // BECAUSE same HEAD-SIGN for different trips
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Schools", // Central Memorial
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "West Spgs") // West Spgs
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList( //
								"8396", // NB Old Banff Coach RD @ Coach Hill RD SW <=
								"3561", // !=
								"3786", // <>
								"8822", // <> NB 77 ST SW @ Old Banff Coach RD <=
								"3706", // <>
								"4922", // !=
								"5613", // ++
								"4690" //  EB 50 AV SW @ 21A ST SW
						)) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList( //
								"4791", // WB 50 AV SW @ 22 ST SW
								"6344", // NB 37 ST SW @ RICHMOND RD
								"5130", // WB 17 AV SW @ 38 ST SW
								"4925", // !=
								"3786", // <>
								"8822", // <> NB 77 ST SW @ Old Banff Coach RD =>
								"3706", // <>
								"4924", // != EB Old Banff Coach RD @ 73 St SW =>
								"6499" // SB  Old Banff Coach RD @ Coach Hill RD SW =>
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(702L, new RouteTripSpec(702L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, NOLAN_HILL, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CHURCHILL) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"5598", // NB Northland DR @ 52 AV NW
								"2039", // WB Nolan Hill Blvd @ Shaganappi Tr NW
								"2048", // ==
								"2049", // !=
								"2050", // !=
								"2051", // ==
								"2034" // EB Nolan Hill Blvd farside Nolan Hill Dr
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"2034", // EB Nolan Hill Blvd farside Nolan Hill Dr
								"5491" // SB Northland DR @ 52 AV NW
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(703L, new RouteTripSpec(703L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, SHERWOOD, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CHURCHILL) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"5598", // NB Northland DR @ 52 AV NW
								"8176" //  SB Sherwood BV @ Sherwood ST NW
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"8176", // SB Sherwood BV @ Sherwood ST NW
								"5598" //  NB Northland DR @ 52 AV NW
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(704L, new RouteTripSpec(704L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Edgepark Blvd", // HAMPTONS // COUNTRY_HLS
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CHURCHILL) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"5598", // NB Northland DR @ 52 AV NW
								"7707", // Hamptons Bus Terminal WB
								"8544" //  EB Country Hills BV @ Edgebrook BV NW
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"8640", // WB Country Hills BV @ Hamptons DR NW
								"7707", // Hamptons Bus Terminal WB
								"5491" //  SB Northland DR @ 52 AV NW
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(705L, new RouteTripSpec(705L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Edgepark Rise", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CHURCHILL) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"5598", // NB Northland DR @ 52 AV NW
								"6600" //  Edgebrook RI NW Terminal
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"6600", // Edgebrook RI NW Terminal
								"5491" //  SB Northland DR @ 52 AV NW
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(706L, new RouteTripSpec(706L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, HAMPTONS, // "Edenwold Dr", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CHURCHILL) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"5598", // NB Northland DR @ 52 AV NW
								"7707" //  Hamptons Bus Terminal WB
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"7707", // Hamptons Bus Terminal WB
								"8357", // SB Edgebrook BV @ Country Hills BV NW
								"5491" //  SB Northland DR @ 52 AV NW
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(711L, new RouteTripSpec(711L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, DOUGLAS_GLEN, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, BEAVERBROOK) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList( //
								"4210", // WB 90 AV SE @ Fairmount DR
								"7377" //  SB Mount McKenzie DR @ 130 AV SE
						)) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList( //
								"8318", // NB Douglasdale BV @ 130 AV SE
								"4210" //  WB 90 AV SE @ Fairmount DR
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(712L, new RouteTripSpec(712L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, BEAVERBROOK, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, PARKLAND) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"6468", // SB Parkvalley DR @ W. of Parkridge DR SE
								"4210" //  WB 90 AV SE @ Fairmount DR
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"4826", // SB Fairmount DR @ 88 AV SE
								"6468" //  SB Parkvalley DR @ W. of Parkridge DR SE
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(713L, new RouteTripSpec(713L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, BEAVERBROOK, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, DEER_RUN) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"6270", // NB 146 AV SE @ Deer Lake RD Terminal
								"8297", // NB Deer Run BV @ Deer Lane CL SE
								"6273", // ++
								"4210" //  WB 90 AV SE @ Fairmount DR
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"4826", // SB Fairmount DR @ 88 AV SE
								"8302", // ++
								"6270", // NB 146 AV SE @ Deer Lake RD Terminal
								"8297" //  NB Deer Run BV @ Deer Lane CL SE
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(715L, new RouteTripSpec(715L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, BEAVERBROOK, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, QUEENSLAND) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"8315", // EB Queensland PL @ Queensland DR SE
								"8316", // WB Queensland PL @ Queensland DR SE
								"8317", // ++
								"4210" //  WB 90 AV SE @ Fairmount DR
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"4826", // SB Fairmount DR @ 88 AV SE
								"9163", // ++
								"8315", // EB Queensland PL @ Queensland DR SE
								"8316" //  WB Queensland PL @ Queensland DR SE
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(719L, new RouteTripSpec(719L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, MC_KENZIE, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, BEAVERBROOK) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList( //
								"4210", // == WB 90 AV SE @ Fairmount DR
								"6510", // !=
								"8319", // !=
								"7377", // ==
								"6970", // WB McKenzie Lake BV @ Mount McKenzie DR SE
								"7051", // !=
								"7039", // <> EB McKenzie DR @ McKenzie Lake BV SE
								"6585" //  <> EB McKenzie Lake BV @ McKenzie Lake WY SE
						)) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList( //
								"7039", // <> EB McKenzie DR @ McKenzie Lake BV SE
								"6585", // <> EB McKenzie Lake BV @ McKenzie Lake WY SE
								"7682", // !=
								"7376", // ==
								"8318", // !=
								"4899", // !=
								"4896", // !=
								"4210" //  == WB 90 AV SE @ Fairmount DR
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(722L, new RouteTripSpec(722L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, BOWNESS, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, TUSCANY) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList( //
								"4127", // WB Tuscany Ravine RD @ Tuscany Ravine HT NW
								"4060" // SB 77 ST NW @ 46 AV NW
						)) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList( //
								"4060", // SB 77 ST NW @ 46 AV NW
								"4127" // WB Tuscany Ravine RD @ Tuscany Ravine HT NW
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(723L, new RouteTripSpec(723L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, BOWNESS, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, TUSCANY) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList( //
								"9594", // <> SB Tuscany DR @ Toscana GD NW
								"4718", // <> EB Tuscany Estates DR @ Tuscany Glen PA NW
								"6875", // !=
								"4060" //  SB 77 ST NW @ 46 AV NW
						)) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList( //
								"4060", // SB 77 ST NW @ 46 AV NW
								"3834", // !=
								"9594", // <> SB Tuscany DR @ Toscana GD NW
								"4718" //  <> EB Tuscany Estates DR @ Tuscany Glen PA NW
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(724L, new RouteTripSpec(724L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, TUSCANY, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, BOWNESS) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"4060", // SB 77 ST NW @ 46 AV NW
								"4061", // ++
								"6731", // ++
								"8432", // NB Tuscany Springs BV @ Tuscany BV NW
								"3822", // SB Tuscany WY @ Tuscany Ridge HT NW
								"8003" //  EB Tuscany BV @ Tuscany Hills RD NW
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"8432", // NB Tuscany Springs BV @ Tuscany BV NW
								"3822", // SB Tuscany WY @ Tuscany Ridge HT NW
								"8003", // EB Tuscany BV @ Tuscany Hills RD NW
								"5179", // ++
								"6317", // ++
								"4060" //  SB 77 ST NW @ 46 AV NW
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(725L, new RouteTripSpec(725L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, SILVER_SPGS, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, BOWNESS) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"4060", // SB 77 ST NW @ 46 AV NW
								"5824" //
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"5811", //
								"4060" //  SB 77 ST NW @ 46 AV NW
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(731L, new RouteTripSpec(731L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, RIVERBEND, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, CENTRAL_MEMORIAL) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList( //
								"4083", // SB 21 ST SW @ 51 AV SW
								"2470", // SB 18 St @ Rivervalley Dr SE
								"8058", // ++
								"6909", // SB 18 ST SE @ Riverview CL
								"8064" //  NB 18 ST SE @ Riverglen DR
						)) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList( //
								"2470", // SB 18 St @ Rivervalley Dr SE
								"6909", // SB 18 ST SE @ Riverview CL
								"8064", // NB 18 ST SE @ Riverglen DR
								"4690", // ++
								"4083" //  SB 21 ST SW @ 51 AV SW
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(738L, new RouteTripSpec(738L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, PANORAMA_HLS_NORTH, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, DIEFENBAKER) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"5783", // NB 4 ST NW @ 68 AV NW (JG Diefenbaker HS)
								"4114", // WB Panatela GA @ Harvest Hills BV NE
								"4414", // WB Panatella BV @ Panatella ST NW
								"9703", // SB Panamount BV @ S. of Panamount PZ NW
								"4304", // ==
								"4095", // !=
								"4094", // !=
								"4093", // ==
								"4100" //  SB Country Village LI @ Country Village WY NE
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"4114", // WB Panatela GA @ Harvest Hills BV NE
								"4414", // WB Panatella BV @ Panatella ST NW
								"9703", // SB Panamount BV @ S. of Panamount PZ NW
								"8421", // ++
								"5783" //  NB 4 ST NW @ 68 AV NW (JG Diefenbaker HS)
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(820L, new RouteTripSpec(820L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, ST_FRANCIS, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, SCENIC_ACRES_NORTH) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList( //
								"7159", // NB Scurfield DR @ Scripps LD NW
								"8840" //  EB Northmont DR @ Calandar RD NW
						)) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList( //
								"8976", // WB Northmount DR @ Clarendon RD NW
								"5222" //  EB Scenic Acres BV @ Scenic Acres DR NW
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(882L, new RouteTripSpec(882L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Nolan Hl", //
				1, MTrip.HEADSIGN_TYPE_STRING, StringUtils.EMPTY) //
				.addTripSort(0, //
						Arrays.asList( //
								"3864", // SB Sherwood BV @ Sherwood WY NW
								"2070", // ++
								"2034" //  EB Nolan Hill BV @ Nolan Hill DR farside
						)) //
				.addTripSort(1, //
						Collections.emptyList()) //
				.compileBothTripSort());
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public int compareEarly(long routeId,
							@NotNull List<MTripStop> list1, @NotNull List<MTripStop> list2,
							@NotNull MTripStop ts1, @NotNull MTripStop ts2,
							@NotNull GStop ts1GStop, @NotNull GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop, this);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@NotNull
	@Override
	public ArrayList<MTrip> splitTrip(@NotNull MRoute mRoute, @Nullable GTrip gTrip, @NotNull GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return ALL_ROUTE_TRIPS2.get(mRoute.getId()).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@NotNull
	@Override
	public Pair<Long[], Integer[]> splitTripStop(@NotNull MRoute mRoute, @NotNull GTrip gTrip, @NotNull GTripStop gTripStop, @NotNull ArrayList<MTrip> splitTrips, @NotNull GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()), this);
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	@Override
	public void setTripHeadsign(@NotNull MRoute mRoute, @NotNull MTrip mTrip, @NotNull GTrip gTrip, @NotNull GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return; // split
		}
		final String tripHeadsign = gTrip.getTripHeadsignOrDefault();
		if (isGoodEnoughAcceptedForSchoolsRoutes(mRoute.getId())) { // School routes
			int directionId = gTrip.getDirectionId() == null ? 0 : gTrip.getDirectionId();
			mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign) + (directionId == 0 ? "" : SPACE), directionId);
			return;
		}
		mTrip.setHeadsignString(
				cleanTripHeadsign(tripHeadsign),
				gTrip.getDirectionIdOrDefault()
		);
	}

	@Override
	public boolean mergeHeadsign(@NotNull MTrip mTrip, @NotNull MTrip mTripToMerge) {
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
		} else if (mTrip.getRouteId() == 2L) {
			if (Arrays.asList( //
					"78th Ave Terminal", //
					CITY_CTR, // <>
					MOUNT_PLEASANT // <>
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(MOUNT_PLEASANT, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					CITY_CTR, // <>
					MOUNT_PLEASANT, // <>
					KILLARNEY_SLASH_17_AVE_SW //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(KILLARNEY_SLASH_17_AVE_SW, mTrip.getHeadsignId());
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
					CHINOOK_STATION //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CHINOOK_STATION, mTrip.getHeadsignId());
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
		} else if (mTrip.getRouteId() == 13L) {
			if (Arrays.asList( //
					MOUNT_ROYAL, // <>
					WESTHILLS //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(WESTHILLS, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					MOUNT_ROYAL, // <>
					CITY_CTR //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CITY_CTR, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 14L) {
			if (Arrays.asList( //
					SOMERSET + DASH + BRIDLEWOOD_STATION, //
					CRANSTON //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CRANSTON, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					BRIDLEWOOD + _SLASH_ + SOMERSET_STATION, // <>
					CRANSTON + _SLASH_ + SOMERSET_STATION //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CRANSTON + _SLASH_ + SOMERSET_STATION, mTrip.getHeadsignId());
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
					UNIVERSITY_OF_CALGARY, //
					NORTHMOUNT_DR_N // <>
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(NORTHMOUNT_DR_N, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					NORTHMOUNT_DR_N, // <>
					HERITAGE_STATION //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(HERITAGE_STATION, mTrip.getHeadsignId());
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
		} else if (mTrip.getRouteId() == 24L) {
			if (Arrays.asList( //
					"Quarry Pk", //
					CITY_CTR //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CITY_CTR, mTrip.getHeadsignId());
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
					WHITEHORN_STATION, //
					"Temple" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Temple", mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					WHITEHORN_STATION, //
					BRENTWOOD_STATION //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(BRENTWOOD_STATION, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"Temple", // <>
					"Brentwood" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Brentwood", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 41L) {
			if (Arrays.asList( //
					CHINOOK_STATION, //
					"Lynnwood" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Lynnwood", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 57L) {
			if (Arrays.asList( //
					WHITEHORN_STATION, // <>
					"Monterey Pk" // <>
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Monterey Pk", mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					WHITEHORN_STATION, // <>
					"Monterey Pk", // <>
					MCCALL_WAY_NE //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(MCCALL_WAY_NE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 81L) {
			if (Arrays.asList( //
					"Macleod Tr N", // <>
					"Macleod Tr S" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Macleod Tr S", mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					NORTH, // <>
					SOUTH //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SOUTH, mTrip.getHeadsignId());
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
					_69_ST_STATION, // <>
					COACH_HILL // <>
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(COACH_HILL, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					_69_ST_STATION, // <>
					COACH_HILL, // <>
					WESTBROOK_STATION //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(WESTBROOK_STATION, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 94L) {
			if (Arrays.asList( //
					_69_ST_STATION, //
					WESTBROOK //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(WESTBROOK, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					_69_ST_STATION, //
					WESTHILLS //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(WESTHILLS, mTrip.getHeadsignId());
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
		} else if (mTrip.getRouteId() == 99L) {
			if (Arrays.asList( //
					"West", //
					"Oakridge" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Oakridge", mTrip.getHeadsignId());
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
		} else if (mTrip.getRouteId() == 112L) {
			if (Arrays.asList( //
					CITY_CTR, //
					"Sarcee Rd" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Sarcee Rd", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 127L) {
			if (Arrays.asList( //
					MARLBOROUGH_STATION, //
					"Franklin Industrial" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Franklin Industrial", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 134L) {
			if (Arrays.asList( //
					"North Ranchlands", //
					"South Silver Spgs" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("South Silver Spgs", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 135L) {
			if (Arrays.asList( //
					"36 St Se" + _SLASH_ + ERIN_WOODS, // <>
					MARLBOROUGH_STATION //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(MARLBOROUGH_STATION, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 150L) {
			if (Arrays.asList( //
					"114 Ave Se", // <>
					ANDERSON //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ANDERSON, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 152L) {
			if (Arrays.asList( //
					"114 Ave Se", //
					"New Brighton" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("New Brighton", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 300L) {
			if (Arrays.asList( //
					CITY_CTR, // <>
					AIRPORT //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(AIRPORT, mTrip.getHeadsignId());
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
					"Auburn Bay", // <>
					SOMERSET + "-" + BRIDLEWOOD + " Sta" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SOMERSET + "-" + BRIDLEWOOD + " Sta", mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"Auburn Bay", // <>
					"McKenzie Towne" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("McKenzie Towne", mTrip.getHeadsignId());
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
					EAST_HILLS + _SLASH_ + CHATEAU_EST, //
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
		} else if (mTrip.getRouteId() == 753L) {
			if (Arrays.asList( //
					"North", //
					"James Fowler / Evanston" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("James Fowler / Evanston", mTrip.getHeadsignId());
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
		} else if (mTrip.getRouteId() == 766L) {
			if (Arrays.asList( //
					"Ep Scarlett Evergreen" + SPACE, //
					SOUTH + SPACE //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SOUTH + SPACE, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"Ep Scarlett Evergreen", //
					NORTH //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(NORTH, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 795L) {
			if (Arrays.asList( //
					"F E Osborne / Sage Hl" + SPACE, //
					SOUTH + SPACE //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SOUTH + SPACE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 810L) {
			if (Arrays.asList( //
					ST_MARGARET + SPACE + ST_FRANCIS + _SLASH_ + NORTH_POINTE, //
					ST_MARGARET + SPACE + NORTH_POINTE, //
					NORTH //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(NORTH, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 845L) {
			if (Arrays.asList( //
					"Msgr Js Smith / Mahogany", //
					NORTH //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(NORTH, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"Msgr Js Smith / Mahogany" + SPACE, //
					SOUTH + SPACE //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SOUTH + SPACE, mTrip.getHeadsignId());
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
		throw new MTLog.Fatal("Unexpected trips to merge %s & %s!", mTrip, mTripToMerge);
	}

	private static final Pattern AVENUE_ = Pattern.compile("((^|\\W)(av)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String AVENUE_REPLACEMENT = "$2Avenue$4";

	private static final Pattern MRU_ = Pattern.compile("((^|\\W)(mru|mount royal university)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String MRU_REPLACEMENT = "$2" + MRU + "$4";

	private static final Pattern MC_KENZIE_ = Pattern.compile("((^|\\W)(mckenzie)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String MC_KENZIE_REPLACEMENT = "$2" + MC_KENZIE + "$4";

	private static final Pattern STN = Pattern.compile("((^|\\W)(stn)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String STN_REPLACEMENT = "$2Station$4";

	private static final Pattern ENDS_WITH_EXPRESS = Pattern.compile("((\\W)(express)($))", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_BRT = Pattern.compile("((^)(brt)(\\W))", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_MAX_NAME_ = Pattern.compile("((^)(max [\\w]+)(\\W))", Pattern.CASE_INSENSITIVE);

	private static final Pattern ROUTE_RSN = Pattern.compile("((^)(route )?([\\d]+)($))", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		if (Utils.isUppercaseOnly(tripHeadsign, true, true)) {
			tripHeadsign = tripHeadsign.toLowerCase(Locale.ENGLISH);
		}
		tripHeadsign = CleanUtils.keepToAndRemoveVia(tripHeadsign);
		tripHeadsign = AVENUE_.matcher(tripHeadsign).replaceAll(AVENUE_REPLACEMENT);
		tripHeadsign = STARTS_WITH_BRT.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = STARTS_WITH_MAX_NAME_.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = MRU_.matcher(tripHeadsign).replaceAll(MRU_REPLACEMENT);
		tripHeadsign = MC_KENZIE_.matcher(tripHeadsign).replaceAll(MC_KENZIE_REPLACEMENT);
		tripHeadsign = STN.matcher(tripHeadsign).replaceAll(STN_REPLACEMENT);
		tripHeadsign = ROUTE_RSN.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = ENDS_WITH_EXPRESS.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = CleanUtils.cleanSlashes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern ENDS_WITH_BOUND = Pattern.compile("([\\s]*[sewn]b[\\s]$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_BOUND = Pattern.compile("(^[\\s]*[sewn]b[\\s]*)", Pattern.CASE_INSENSITIVE);

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

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
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
