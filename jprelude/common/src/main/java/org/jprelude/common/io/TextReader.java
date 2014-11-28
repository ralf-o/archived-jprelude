package org.jprelude.common.io;
import java.io.*;
import java.util.Iterator;
import org.jprelude.common.util.Generator;
import org.jprelude.common.util.Seq;

public final class TextReader {
  private Seq<String> seq = null;
  
  public TextReader(final InputStream input) { 
    this.seq = Seq.from(new Iterable() {
        @Override
        public Iterator<String> iterator() {
          return new Generator<String>() {
            private boolean reachedEnd = false;
            private BufferedReader bufferedReader;

            @Override
            protected void generate() {
              String line;

              if (!this.reachedEnd) {
                try {
                  if (this.bufferedReader == null) {
                    this.bufferedReader = new BufferedReader(new InputStreamReader(input));
                  }

                  line = this.bufferedReader.readLine();
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }            

                if (line == null) {
                  try {
                    this.bufferedReader.close();
                  } catch (final IOException e) {
                  }

                  this.reachedEnd = true;
                } else {
                  this.yield(line);
                }
              }
            }

            //@Override
            protected void clear() {
              if (this.bufferedReader != null) {
                try {
                  this.bufferedReader.close();
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }

                this.bufferedReader = null;
              }
            }
          };
        }
      });
  }

  public TextReader(final File file) {
    this.seq = Seq.from(new Iterable() {
        @Override
        public Iterator<String> iterator() {
          return new Generator<String>() {
            private boolean reachedEnd = false;
            private BufferedReader bufferedReader;

            @Override
            protected void generate() {
              String line;

              if (!this.reachedEnd) {
                try {
                  if (this.bufferedReader == null) {
                    this.bufferedReader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(file))));
                  }

                  line = this.bufferedReader.readLine();
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }            

                if (line == null) {
                  try {
                    this.bufferedReader.close();
                  } catch (IOException ignore) {
                  }

                  this.reachedEnd = true;
                } else {
                  this.yield(line);
                }
              }
            }

            //@Override
            protected void clear() {
              if (this.bufferedReader != null) {
                try {
                  this.bufferedReader.close();
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }

                this.bufferedReader = null;
              }
            }
          };
        }
      });
  }
  /*
  public static TextReader of(final FileObject file) {
    TextReader ret = new TextReader();
    
    ret.seq = Seq.of(new Iterable() {
      public Iterator<String> iterator() {
        return new Generator<String>() {
          private boolean reachedEnd = false;
          private BufferedReader bufferedReader;

          protected void generate() {
            String line;

            if (!this.reachedEnd) {
              try {
                if (this.bufferedReader == null) {
                 this.bufferedReader = new BufferedReader(new InputStreamReader(new DataInputStream(file.getContent().getInputStream())));
                }

                line = this.bufferedReader.readLine();
              } catch (IOException e) {
                throw new RuntimeException(e);
              }            

              if (line == null) {
                try {
                  this.bufferedReader.close();
                } catch (IOException _) {
                }

                this.reachedEnd = true;
              } else {
                this.yield(line);
              }
            }
          }
  
          //@Override
          protected void clear() {
            if (this.bufferedReader != null) {
              try {
                this.bufferedReader.close();
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
              
              this.bufferedReader = null;
            }
          }
        };
      }
    });
  
    return ret;
  }
  */
  public Seq<String> readLines() {
    return this.seq;
  }
/*
  public Observable<String> toObservable() {
    return this.seq.toObservable();
  }
  */
}