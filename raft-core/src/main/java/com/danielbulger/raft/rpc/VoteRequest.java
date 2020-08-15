/**
 * Autogenerated by Thrift Compiler (0.10.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.danielbulger.raft.rpc;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.10.0)", date = "2020-08-04")
public class VoteRequest implements org.apache.thrift.TBase<VoteRequest, VoteRequest._Fields>, java.io.Serializable, Cloneable, Comparable<VoteRequest> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("VoteRequest");

  private static final org.apache.thrift.protocol.TField TERM_FIELD_DESC = new org.apache.thrift.protocol.TField("term", org.apache.thrift.protocol.TType.I64, (short)1);
  private static final org.apache.thrift.protocol.TField CANDIDATE_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("candidateId", org.apache.thrift.protocol.TType.I32, (short)2);
  private static final org.apache.thrift.protocol.TField LAST_LOG_INDEX_FIELD_DESC = new org.apache.thrift.protocol.TField("lastLogIndex", org.apache.thrift.protocol.TType.I64, (short)3);
  private static final org.apache.thrift.protocol.TField LAST_LOG_TERM_FIELD_DESC = new org.apache.thrift.protocol.TField("lastLogTerm", org.apache.thrift.protocol.TType.I64, (short)4);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new VoteRequestStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new VoteRequestTupleSchemeFactory();

  public long term; // required
  public int candidateId; // required
  public long lastLogIndex; // required
  public long lastLogTerm; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    TERM((short)1, "term"),
    CANDIDATE_ID((short)2, "candidateId"),
    LAST_LOG_INDEX((short)3, "lastLogIndex"),
    LAST_LOG_TERM((short)4, "lastLogTerm");

    private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

    static {
      for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // TERM
          return TERM;
        case 2: // CANDIDATE_ID
          return CANDIDATE_ID;
        case 3: // LAST_LOG_INDEX
          return LAST_LOG_INDEX;
        case 4: // LAST_LOG_TERM
          return LAST_LOG_TERM;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(java.lang.String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final java.lang.String _fieldName;

    _Fields(short thriftId, java.lang.String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public java.lang.String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __TERM_ISSET_ID = 0;
  private static final int __CANDIDATEID_ISSET_ID = 1;
  private static final int __LASTLOGINDEX_ISSET_ID = 2;
  private static final int __LASTLOGTERM_ISSET_ID = 3;
  private byte __isset_bitfield = 0;
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.TERM, new org.apache.thrift.meta_data.FieldMetaData("term", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64        , "Term")));
    tmpMap.put(_Fields.CANDIDATE_ID, new org.apache.thrift.meta_data.FieldMetaData("candidateId", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.LAST_LOG_INDEX, new org.apache.thrift.meta_data.FieldMetaData("lastLogIndex", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64        , "LogIndex")));
    tmpMap.put(_Fields.LAST_LOG_TERM, new org.apache.thrift.meta_data.FieldMetaData("lastLogTerm", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64        , "Term")));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(VoteRequest.class, metaDataMap);
  }

  public VoteRequest() {
  }

  public VoteRequest(
    long term,
    int candidateId,
    long lastLogIndex,
    long lastLogTerm)
  {
    this();
    this.term = term;
    setTermIsSet(true);
    this.candidateId = candidateId;
    setCandidateIdIsSet(true);
    this.lastLogIndex = lastLogIndex;
    setLastLogIndexIsSet(true);
    this.lastLogTerm = lastLogTerm;
    setLastLogTermIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public VoteRequest(VoteRequest other) {
    __isset_bitfield = other.__isset_bitfield;
    this.term = other.term;
    this.candidateId = other.candidateId;
    this.lastLogIndex = other.lastLogIndex;
    this.lastLogTerm = other.lastLogTerm;
  }

  public VoteRequest deepCopy() {
    return new VoteRequest(this);
  }

  @Override
  public void clear() {
    setTermIsSet(false);
    this.term = 0;
    setCandidateIdIsSet(false);
    this.candidateId = 0;
    setLastLogIndexIsSet(false);
    this.lastLogIndex = 0;
    setLastLogTermIsSet(false);
    this.lastLogTerm = 0;
  }

  public long getTerm() {
    return this.term;
  }

  public VoteRequest setTerm(long term) {
    this.term = term;
    setTermIsSet(true);
    return this;
  }

  public void unsetTerm() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __TERM_ISSET_ID);
  }

  /** Returns true if field term is set (has been assigned a value) and false otherwise */
  public boolean isSetTerm() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __TERM_ISSET_ID);
  }

  public void setTermIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __TERM_ISSET_ID, value);
  }

  public int getCandidateId() {
    return this.candidateId;
  }

  public VoteRequest setCandidateId(int candidateId) {
    this.candidateId = candidateId;
    setCandidateIdIsSet(true);
    return this;
  }

  public void unsetCandidateId() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __CANDIDATEID_ISSET_ID);
  }

  /** Returns true if field candidateId is set (has been assigned a value) and false otherwise */
  public boolean isSetCandidateId() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __CANDIDATEID_ISSET_ID);
  }

  public void setCandidateIdIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __CANDIDATEID_ISSET_ID, value);
  }

  public long getLastLogIndex() {
    return this.lastLogIndex;
  }

  public VoteRequest setLastLogIndex(long lastLogIndex) {
    this.lastLogIndex = lastLogIndex;
    setLastLogIndexIsSet(true);
    return this;
  }

  public void unsetLastLogIndex() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __LASTLOGINDEX_ISSET_ID);
  }

  /** Returns true if field lastLogIndex is set (has been assigned a value) and false otherwise */
  public boolean isSetLastLogIndex() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __LASTLOGINDEX_ISSET_ID);
  }

  public void setLastLogIndexIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __LASTLOGINDEX_ISSET_ID, value);
  }

  public long getLastLogTerm() {
    return this.lastLogTerm;
  }

  public VoteRequest setLastLogTerm(long lastLogTerm) {
    this.lastLogTerm = lastLogTerm;
    setLastLogTermIsSet(true);
    return this;
  }

  public void unsetLastLogTerm() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __LASTLOGTERM_ISSET_ID);
  }

  /** Returns true if field lastLogTerm is set (has been assigned a value) and false otherwise */
  public boolean isSetLastLogTerm() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __LASTLOGTERM_ISSET_ID);
  }

  public void setLastLogTermIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __LASTLOGTERM_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, java.lang.Object value) {
    switch (field) {
    case TERM:
      if (value == null) {
        unsetTerm();
      } else {
        setTerm((java.lang.Long)value);
      }
      break;

    case CANDIDATE_ID:
      if (value == null) {
        unsetCandidateId();
      } else {
        setCandidateId((java.lang.Integer)value);
      }
      break;

    case LAST_LOG_INDEX:
      if (value == null) {
        unsetLastLogIndex();
      } else {
        setLastLogIndex((java.lang.Long)value);
      }
      break;

    case LAST_LOG_TERM:
      if (value == null) {
        unsetLastLogTerm();
      } else {
        setLastLogTerm((java.lang.Long)value);
      }
      break;

    }
  }

  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case TERM:
      return getTerm();

    case CANDIDATE_ID:
      return getCandidateId();

    case LAST_LOG_INDEX:
      return getLastLogIndex();

    case LAST_LOG_TERM:
      return getLastLogTerm();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case TERM:
      return isSetTerm();
    case CANDIDATE_ID:
      return isSetCandidateId();
    case LAST_LOG_INDEX:
      return isSetLastLogIndex();
    case LAST_LOG_TERM:
      return isSetLastLogTerm();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof VoteRequest)
      return this.equals((VoteRequest)that);
    return false;
  }

  public boolean equals(VoteRequest that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_term = true;
    boolean that_present_term = true;
    if (this_present_term || that_present_term) {
      if (!(this_present_term && that_present_term))
        return false;
      if (this.term != that.term)
        return false;
    }

    boolean this_present_candidateId = true;
    boolean that_present_candidateId = true;
    if (this_present_candidateId || that_present_candidateId) {
      if (!(this_present_candidateId && that_present_candidateId))
        return false;
      if (this.candidateId != that.candidateId)
        return false;
    }

    boolean this_present_lastLogIndex = true;
    boolean that_present_lastLogIndex = true;
    if (this_present_lastLogIndex || that_present_lastLogIndex) {
      if (!(this_present_lastLogIndex && that_present_lastLogIndex))
        return false;
      if (this.lastLogIndex != that.lastLogIndex)
        return false;
    }

    boolean this_present_lastLogTerm = true;
    boolean that_present_lastLogTerm = true;
    if (this_present_lastLogTerm || that_present_lastLogTerm) {
      if (!(this_present_lastLogTerm && that_present_lastLogTerm))
        return false;
      if (this.lastLogTerm != that.lastLogTerm)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(term);

    hashCode = hashCode * 8191 + candidateId;

    hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(lastLogIndex);

    hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(lastLogTerm);

    return hashCode;
  }

  @Override
  public int compareTo(VoteRequest other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetTerm()).compareTo(other.isSetTerm());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTerm()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.term, other.term);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetCandidateId()).compareTo(other.isSetCandidateId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetCandidateId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.candidateId, other.candidateId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetLastLogIndex()).compareTo(other.isSetLastLogIndex());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetLastLogIndex()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.lastLogIndex, other.lastLogIndex);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetLastLogTerm()).compareTo(other.isSetLastLogTerm());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetLastLogTerm()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.lastLogTerm, other.lastLogTerm);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    scheme(iprot).read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    scheme(oprot).write(oprot, this);
  }

  @Override
  public java.lang.String toString() {
    java.lang.StringBuilder sb = new java.lang.StringBuilder("VoteRequest(");
    boolean first = true;

    sb.append("term:");
    sb.append(this.term);
    first = false;
    if (!first) sb.append(", ");
    sb.append("candidateId:");
    sb.append(this.candidateId);
    first = false;
    if (!first) sb.append(", ");
    sb.append("lastLogIndex:");
    sb.append(this.lastLogIndex);
    first = false;
    if (!first) sb.append(", ");
    sb.append("lastLogTerm:");
    sb.append(this.lastLogTerm);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class VoteRequestStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public VoteRequestStandardScheme getScheme() {
      return new VoteRequestStandardScheme();
    }
  }

  private static class VoteRequestStandardScheme extends org.apache.thrift.scheme.StandardScheme<VoteRequest> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, VoteRequest struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // TERM
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.term = iprot.readI64();
              struct.setTermIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // CANDIDATE_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.candidateId = iprot.readI32();
              struct.setCandidateIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // LAST_LOG_INDEX
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.lastLogIndex = iprot.readI64();
              struct.setLastLogIndexIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // LAST_LOG_TERM
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.lastLogTerm = iprot.readI64();
              struct.setLastLogTermIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, VoteRequest struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(TERM_FIELD_DESC);
      oprot.writeI64(struct.term);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(CANDIDATE_ID_FIELD_DESC);
      oprot.writeI32(struct.candidateId);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(LAST_LOG_INDEX_FIELD_DESC);
      oprot.writeI64(struct.lastLogIndex);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(LAST_LOG_TERM_FIELD_DESC);
      oprot.writeI64(struct.lastLogTerm);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class VoteRequestTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public VoteRequestTupleScheme getScheme() {
      return new VoteRequestTupleScheme();
    }
  }

  private static class VoteRequestTupleScheme extends org.apache.thrift.scheme.TupleScheme<VoteRequest> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, VoteRequest struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetTerm()) {
        optionals.set(0);
      }
      if (struct.isSetCandidateId()) {
        optionals.set(1);
      }
      if (struct.isSetLastLogIndex()) {
        optionals.set(2);
      }
      if (struct.isSetLastLogTerm()) {
        optionals.set(3);
      }
      oprot.writeBitSet(optionals, 4);
      if (struct.isSetTerm()) {
        oprot.writeI64(struct.term);
      }
      if (struct.isSetCandidateId()) {
        oprot.writeI32(struct.candidateId);
      }
      if (struct.isSetLastLogIndex()) {
        oprot.writeI64(struct.lastLogIndex);
      }
      if (struct.isSetLastLogTerm()) {
        oprot.writeI64(struct.lastLogTerm);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, VoteRequest struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet incoming = iprot.readBitSet(4);
      if (incoming.get(0)) {
        struct.term = iprot.readI64();
        struct.setTermIsSet(true);
      }
      if (incoming.get(1)) {
        struct.candidateId = iprot.readI32();
        struct.setCandidateIdIsSet(true);
      }
      if (incoming.get(2)) {
        struct.lastLogIndex = iprot.readI64();
        struct.setLastLogIndexIsSet(true);
      }
      if (incoming.get(3)) {
        struct.lastLogTerm = iprot.readI64();
        struct.setLastLogTermIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

