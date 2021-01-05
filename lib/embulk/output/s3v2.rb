Embulk::JavaPlugin.register_output(
  :s3v2,
  'org.embulk.output.s3v2.S3V2FileOutputPlugin',
  File.expand_path('../../../../classpath', __FILE__)
)