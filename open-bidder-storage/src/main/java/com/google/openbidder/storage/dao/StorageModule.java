package com.google.openbidder.storage.dao;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.openbidder.storage.utils.Converter;
import com.google.openbidder.storage.utils.ProtobufConverter;
import com.google.protobuf.MessageLite;

/**
 * Bind DAO and converter to implementations.
 */
public class StorageModule extends AbstractModule {

  @Override
  public void configure() {
    bind(new TypeLiteral<Dao<MessageLite>>(){})
        .to(new TypeLiteral<CloudStorageDao<MessageLite>>(){});
    bind(new TypeLiteral<Converter<MessageLite>>(){}).to(ProtobufConverter.class);
  }
}
