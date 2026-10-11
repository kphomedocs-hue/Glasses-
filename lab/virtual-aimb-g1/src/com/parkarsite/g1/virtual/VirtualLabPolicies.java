package com.parkarsite.g1.virtual;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;

/**
 * Offline-only candidate application policies. Zero socket, Android or glasses operations.
 * Simulated evidence MUST NOT be interpreted as authenticated physical firmware behaviour.
 * The frozen production implementation has not yet been wired to these decisions.
 */
public final class VirtualLabPolicies {
  private VirtualLabPolicies(){}
  public static final long MEDIA_CAP=33_554_432L;
  public static final int CATALOG_CAP=65_536;
  public enum MediaKind { JPG, OPUS, MP4 }
  public record Entry(String path,MediaKind kind){}
  public enum Decision { ALLOW, BLOCK, AMBIGUOUS, EXPIRED, REMOTE_UNCERTAIN, MANUAL_REVIEW }
  public static void require(boolean valid){if(!valid)throw new IllegalArgumentException("Unsafe synthetic input");}

  /** A separate group cleanup model. The real Android adapter has not been implemented. */
  public static final class Group {
    public enum State { NEW, DISCOVERING, CANDIDATE, GROUP_VERIFIED, CLEANUP_PENDING, CLEAN, UNCERTAIN, ABORTED }
    public enum CleanupStatus { SUCCESS, BUSY, FAILURE }
    private final VirtualClock clock;
    private State state=State.NEW;
    private int removals;
    private boolean groupSeen, memberProven, networkBound;
    public Group(VirtualClock clock){this.clock=Objects.requireNonNull(clock);}
    public State state(){return state;}
    public int removals(){return removals;}
    public boolean mayCatalog(){return state==State.GROUP_VERIFIED && memberProven && networkBound;}
    public boolean mayConnectRealPeer(){return false;}
    public void preflight(boolean preexistingGroup,boolean testAdapterApproved){
      if(state!=State.NEW)return;
      if(preexistingGroup||!testAdapterApproved){state=State.ABORTED;return;}
      state=State.DISCOVERING;
      clock.deadline("group-discovery",16_000,()->{if(state==State.DISCOVERING||state==State.CANDIDATE)cleanup();});
    }
    public void peers(int plausible){
      if(state!=State.DISCOVERING)return;
      if(plausible==1)state=State.CANDIDATE;
      else if(plausible>1)cleanup();
    }
    /** Simulated membership and local Network binding; NOT device authentication. */
    public void formed(boolean selectedMember,boolean bound){
      if(state!=State.CANDIDATE)return;
      groupSeen=true;memberProven=selectedMember;networkBound=bound;
      if(memberProven&&networkBound){clock.cancel("group-discovery");state=State.GROUP_VERIFIED;}
      else cleanup();
    }
    public void activityDestroyed(){if(state!=State.CLEAN&&state!=State.ABORTED)cleanup();}
    public void cleanup(){
      if(state==State.NEW){state=State.ABORTED;return;}
      if(state==State.CLEAN||state==State.UNCERTAIN||state==State.ABORTED||state==State.CLEANUP_PENDING)return;
      clock.cancel("group-discovery");state=State.CLEANUP_PENDING;
      removals++;
      clock.deadline("group-cleanup",5_000,()->{if(state==State.CLEANUP_PENDING)state=State.UNCERTAIN;});
    }
    public void cleanupCallback(CleanupStatus result,boolean groupAbsent){
      if(state!=State.CLEANUP_PENDING)return;
      // Callback SUCCESS alone is not proof; if group remains then uncertain.
      clock.cancel("group-cleanup");
      state=groupAbsent?State.CLEAN:State.UNCERTAIN;
      memberProven=false;networkBound=false;
    }
    public void groupChanged(){if(state==State.GROUP_VERIFIED){memberProven=false;networkBound=false;cleanup();}}
  }

  /** Strict synthetic line-list plus an explicitly selected and restricted JSON fixture schema. */
  public static final class Catalog {
    public record Result(List<Entry> entries,int duplicates,int blanks) {
      public int count(MediaKind kind){return (int)entries.stream().filter(e->e.kind()==kind).count();}
    }
    private static final Pattern JSON=Pattern.compile("\\s*\\{\\s*\"files\"\\s*:\\s*\\[(.*)]\\s*}\\s*",Pattern.DOTALL);
    private static final Pattern JSON_ITEM=Pattern.compile("\\s*\\{\\s*\"path\"\\s*:\\s*\"([A-Za-z0-9_./-]+)\"\\s*}\\s*");
    private static String text(byte[] body){
      require(body!=null&&body.length<=CATALOG_CAP);
      try{return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
              .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(body)).toString();}
      catch(CharacterCodingException ex){throw new IllegalArgumentException("Malformed catalog");}
    }
    public static Result parse(int configType,byte[] body){
      String decoded=text(body);
      List<String> lines=new ArrayList<>();
      if(configType==1){lines.addAll(Arrays.asList(decoded.split("\\r?\\n",-1)));}
      else if(configType==2){
        Matcher m=JSON.matcher(decoded);
        require(m.matches());
        String inner=m.group(1).trim();
        if(!inner.isEmpty()){
          // Only the synthetic {files:[{path:"..."}]} schema; reject other fields/escapes.
          for(String item:inner.split("(?<=}),\\s*(?=\\{)",-1)){
            Matcher match=JSON_ITEM.matcher(item);require(match.matches());
            lines.add(match.group(1));
          }
        }
      } else throw new IllegalArgumentException("Unknown configFileType");
      List<Entry> entries=new ArrayList<>();Set<String> seen=new HashSet<>();
      int duplicate=0,blank=0;
      for(String line:lines){
        String path=line.trim();
        if(path.isEmpty()){blank++;continue;}
        require(path.matches("[A-Za-z0-9_./-]{1,200}")
          &&!path.startsWith("/")&&!path.startsWith(".")&&!path.contains("..")
          &&!path.contains("//")&&!path.contains("\\")&&!path.contains(":"));
        MediaKind kind=kindOf(path);
        require(kind!=null);
        if(!seen.add(path)){duplicate++;continue;}
        entries.add(new Entry(path,kind));
      }
      return new Result(List.copyOf(entries),duplicate,blank);
    }
    public static MediaKind kindOf(String path){
      if(path==null)return null;
      if(path.endsWith(".jpg"))return MediaKind.JPG;
      if(path.endsWith(".mp4"))return MediaKind.MP4;
      if(path.endsWith(".opus"))return MediaKind.OPUS;
      return null;
    }
    public static byte[] bytes(String value){return value.getBytes(StandardCharsets.UTF_8);}
  }

  /** One source delta; never equates first/last index or inventory count alone to identity. */
  public static final class Capture {
    private final Set<String> baseline;
    private boolean selected;
    private String selectedPath;
    public Capture(Catalog.Result before){
      require(before.duplicates()==0);
      baseline=new HashSet<>();
      for(Entry e:before.entries())baseline.add(e.path());
    }
    public Decision inspect(Catalog.Result after,Integer expectedImageCount){
      if(after.duplicates()>0)return Decision.AMBIGUOUS;
      Set<String> fresh=new HashSet<>();
      int jpg=0;
      for(Entry e:after.entries()){
        if(e.kind()==MediaKind.JPG)jpg++;
        if(!baseline.contains(e.path()))fresh.add(e.path());
      }
      if(expectedImageCount!=null&&expectedImageCount!=jpg)return Decision.BLOCK;
      Set<String> afterPaths=new HashSet<>();
      for(Entry e:after.entries())afterPaths.add(e.path());
      if(!afterPaths.containsAll(baseline))return Decision.BLOCK; // pre-GET retention is mandatory
      if(fresh.isEmpty())return Decision.EXPIRED;
      if(fresh.size()!=1)return Decision.AMBIGUOUS;
      String path=fresh.iterator().next();
      if(Catalog.kindOf(path)!=MediaKind.JPG)return Decision.BLOCK;
      if(selected&&!selectedPath.equals(path))return Decision.AMBIGUOUS;
      selected=true;selectedPath=path;return Decision.ALLOW;
    }
    public String selected(){return selectedPath;} // local only; never print in reports
  }

  /** Synthetic HTTP response machine; deliberately NO transport or automatic retry API. */
  public static final class OneShot {
    private boolean marked,requestOpened,committed;
    private MediaKind requestedKind;
    private Decision outcome=Decision.BLOCK;
    private long actual;
    public Decision start(boolean networkVerified,boolean policyPermitsRead,
                          String method,String relativePath,boolean hasRange){
      if(marked)return Decision.REMOTE_UNCERTAIN;
      if(!networkVerified||!policyPermitsRead||hasRange||!"GET".equals(method)
           ||Catalog.kindOf(relativePath)==null ||relativePath.startsWith("/")
           ||relativePath.contains("..")||relativePath.contains(":"))return Decision.BLOCK;
      requestedKind=Catalog.kindOf(relativePath);
      marked=true;outcome=Decision.REMOTE_UNCERTAIN;return Decision.ALLOW;
    }
    public boolean marked(){return marked;}
    public boolean committed(){return committed;}
    public long bytes(){return actual;}
    public Decision observe(int status,long expected,byte[] payload,
                            boolean redirected,boolean fullyRead,boolean durableReceipt){
      if(!marked||requestOpened)return Decision.BLOCK;
      requestOpened=true;
      if(status!=200||redirected||expected<=0||expected>MEDIA_CAP
         ||payload==null||payload.length>MEDIA_CAP||!fullyRead)return outcome;
      actual=payload.length;
      if(actual!=expected)return outcome;
      if(Media.validate(requestedKind,payload)==Media.Fidelity.INVALID)return outcome;
      if(!durableReceipt)return outcome;
      committed=true;outcome=Decision.ALLOW;return outcome;
    }
    public Decision result(){return outcome;}
    public Decision restart(){return committed?Decision.ALLOW:marked?Decision.REMOTE_UNCERTAIN:Decision.BLOCK;}
    public Decision retry(){return Decision.BLOCK;}
  }

  /** Shape/metadata validation only; no claim of full decoder fidelity. */
  public static final class Media {
    public enum Fidelity { INVALID, SHAPE_ONLY, CONTAINER_ONLY, DECODER_VERIFIED }
    public static Fidelity validate(MediaKind kind,byte[] body){
      if(body==null||body.length==0||body.length>MEDIA_CAP)return Fidelity.INVALID;
      if(kind==MediaKind.JPG){
        if(body.length<5||(body[0]&255)!=255||(body[1]&255)!=216||(body[2]&255)!=255
          ||(body[body.length-2]&255)!=255||(body[body.length-1]&255)!=217)return Fidelity.INVALID;
        return Fidelity.SHAPE_ONLY;
      }
      if(kind==MediaKind.MP4){
        int pos=0;boolean ftyp=false,mdat=false;
        while(pos<body.length){
          if(body.length-pos<8)return Fidelity.INVALID;
          long size=Integer.toUnsignedLong(ByteBuffer.wrap(body,pos,4).getInt());
          if(size<8||size>body.length-pos)return Fidelity.INVALID;
          String atom=new String(body,pos+4,4,StandardCharsets.US_ASCII);
          if(atom.equals("ftyp"))ftyp=true;
          if(atom.equals("mdat"))mdat=true;
          pos+=(int)size;
        }
        return ftyp&&mdat?Fidelity.CONTAINER_ONLY:Fidelity.INVALID;
      }
      if(kind==MediaKind.OPUS){
        // Synthetic Ogg Opus signature only, not a proof of real AIMB-G1 codec format.
        if(body.length<36||!new String(body,0,4,StandardCharsets.US_ASCII).equals("OggS"))return Fidelity.INVALID;
        return new String(body,28,8,StandardCharsets.US_ASCII).equals("OpusHead")
                   ?Fidelity.CONTAINER_ONLY:Fidelity.INVALID;
      }
      return Fidelity.INVALID;
    }
  }

  /** Memory-only synthetic reservation model; actual durable JPG-only store tested separately. */
  public static final class Names {
    private final Map<String,String> ids=new HashMap<>(),paths=new HashMap<>();
    private final Set<String> receipts=new HashSet<>();
    private final Map<String,Integer> daily=new HashMap<>();
    private final ZoneId zone;
    public Names(ZoneId zone){this.zone=zone;}
    public String reserve(String opaqueId,long firstSeenMs,MediaKind kind){
      require(opaqueId!=null&&opaqueId.matches("[0-9a-f]{64}")&&kind!=null);
      if(ids.containsKey(opaqueId))return ids.get(opaqueId);
      String date=Instant.ofEpochMilli(firstSeenMs).atZone(zone).toLocalDate().toString();
      int sequence=daily.merge(date,1,Integer::sum);
      String extension=kind.name().toLowerCase(Locale.ROOT);
      String path=date+"/"+String.format(Locale.ROOT,"%04d",sequence)+"."+extension;
      ids.put(opaqueId,path);paths.put(path,opaqueId);return path;
    }
    /** Caller must first verify durable local bytes and receipt through RecoveryImportStore. */
    public boolean receiptVerified(String opaqueId,boolean durableReceiptAndBytesValid){
      if(!ids.containsKey(opaqueId)||!durableReceiptAndBytesValid)return false;
      receipts.add(opaqueId);return true;
    }
    public boolean knownReceipt(String opaqueId){return receipts.contains(opaqueId);}
    public Decision verifyIdentity(String opaqueId,String priorRemoteFingerprint,String newRemoteFingerprint){
      if(!ids.containsKey(opaqueId))return Decision.BLOCK;
      return Objects.equals(priorRemoteFingerprint,newRemoteFingerprint)?Decision.ALLOW:Decision.MANUAL_REVIEW;
    }
    public int reservations(){return paths.size();}
  }

  /** Platform observations are *injected*, never read directly from Android. */
  public static final class Platform {
    public enum State { READY, PLATFORM_BLOCKED, ACTIVE, BACKGROUND, REMOTE_UNCERTAIN, DISK_BLOCKED, SCHEMA_BLOCKED }
    private State state=State.READY;
    private boolean startedFetch;
    public State state(){return state;}
    public void begin(boolean blePermission,boolean wifiPermission,boolean locationMode){
      state=blePermission&&wifiPermission&&locationMode?State.ACTIVE:State.PLATFORM_BLOCKED;
    }
    public void background(){if(state==State.ACTIVE)state=State.BACKGROUND;}
    public void permissionLost(){if(state==State.ACTIVE||state==State.BACKGROUND)state=State.PLATFORM_BLOCKED;}
    public void fetchIntent(){if(state==State.ACTIVE||state==State.BACKGROUND)startedFetch=true;}
    public void restart(){state=startedFetch?State.REMOTE_UNCERTAIN:State.READY;}
    public void diskFull(){state=State.DISK_BLOCKED;}
    public void migrate(int version){if(version!=1)state=State.SCHEMA_BLOCKED;}
    public boolean canFetch(){return false;} // originals-preservation gate dominates all platform permissions
  }

  public static final class Release {
    private Release(){}
    public static boolean cleanProduction(boolean hasSimulatorEndpoint,boolean internetPermission,
                                          boolean debugTestMenu,boolean releaseGatePhysicalPassed){
      return !hasSimulatorEndpoint&&!internetPermission&&!debugTestMenu&&releaseGatePhysicalPassed;
    }
    public static boolean allowHttp(String method,boolean hasRange,boolean redirect,String target){
      return "GET".equals(method)&&!hasRange&&!redirect&&target!=null
               &&target.matches("[A-Za-z0-9_./-]{1,200}")&&!target.contains("..")
               &&!target.startsWith("/");
    }
    /** Sanitise before logging, not after creating a public artifact. */
    public static String redact(String raw){
      if(raw==null)return "";
      String clean=raw.replaceAll("(?i)(?:[0-9a-f]{2}:){5}[0-9a-f]{2}","[REDACTED_MAC]");
      clean=clean.replaceAll("(?i)\\b[a-z0-9_.+-]+@[a-z0-9.-]+\\.[a-z]{2,}\\b","[REDACTED_EMAIL]");
      clean=clean.replaceAll("\\b(?:[0-9]{1,3}\\.){3}[0-9]{1,3}\\b","[REDACTED_IP]");
      clean=clean.replaceAll("(?i)\\b(?:ssid|password|token|passphrase)\\s*[:=]\\s*(?:\\\"[^\\\"]*\\\"|'[^']*'|[^\\s,;]+)","[REDACTED_SECRET]");
      clean=clean.replaceAll("(?i)(?:/[^\\s:]+)+\\.(?:jpg|mp4|opus)","[REDACTED_PATH]");
      clean=clean.replaceAll("(?i)\\b(?:[a-z0-9_-]+/)+[a-z0-9_.-]+\\.(?:jpg|mp4|opus)\\b","[REDACTED_PATH]");
      return clean;
    }
    public static boolean safeReport(String report){
      if(report==null)return false;
      return !Pattern.compile("(?i)(?:[0-9a-f]{2}:){5}[0-9a-f]{2}").matcher(report).find()
        &&!report.matches("(?s).*(?:/[^\\s:]+)+\\.(?:jpg|mp4|opus).*")
        &&!Pattern.compile("(?i)\\b[a-z0-9_.+-]+@[a-z0-9.-]+\\.[a-z]{2,}\\b").matcher(report).find()
        &&!Pattern.compile("\\b(?:[0-9]{1,3}\\.){3}[0-9]{1,3}\\b").matcher(report).find()
        &&!Pattern.compile("(?i)\\b(?:ssid|password|token|passphrase)\\s*[:=]").matcher(report).find();
    }
  }
}
