package youi

sealed trait Node ;

case class Element(tagName: String, attrs: Map[String, String], children: Seq[Node]) extends Node;

  


trait Tag {
  require(name != null)

  def name: String
  def apply(attrs: Attribute*)(children: Element*): Tag
}

object Tag {
  def create(name: String): Tag = {
    return new Tag();
  }
}
