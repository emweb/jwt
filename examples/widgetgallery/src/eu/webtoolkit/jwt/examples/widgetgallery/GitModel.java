/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class GitModel extends WAbstractItemModel {
  private static Logger logger = LoggerFactory.getLogger(GitModel.class);

  public static ItemDataRole ContentsRole = ItemDataRole.of(ItemDataRole.User.getValue() + 1);

  public GitModel(final String repository) {
    super();
    this.git_ = new Git();
    this.treeData_ = new ArrayList<GitModel.Tree>();
    this.childPointer_ = new HashMap<GitModel.ChildIndex, Integer>();
    this.git_.setRepositoryPath(repository);
    this.loadRevision("master");
  }

  public void loadRevision(final String revName) {
    Git.ObjectId treeRoot = this.git_.getCommitTree(revName);
    this.layoutAboutToBeChanged().trigger();
    this.treeData_.clear();
    this.childPointer_.clear();
    this.treeData_.add(new GitModel.Tree(-1, -1, treeRoot, this.git_.treeSize(treeRoot)));
    this.layoutChanged().trigger();
  }

  public WModelIndex getParent(final WModelIndex index) {
    if (!(index != null) || index.getInternalId() == 0) {
      return null;
    } else {
      final GitModel.Tree item = this.treeData_.get(index.getInternalId());
      return this.createIndex(item.getIndex(), 0, item.getParentId());
    }
  }

  public WModelIndex getIndex(int row, int column, final WModelIndex parent) {
    int parentId;
    if (!(parent != null)) {
      parentId = 0;
    } else {
      int grandParentId = parent.getInternalId();
      parentId = this.getTreeId(grandParentId, parent.getRow());
    }
    return this.createIndex(row, column, parentId);
  }

  public int getColumnCount(final WModelIndex parent) {
    return 2;
  }

  public int getRowCount(final WModelIndex parent) {
    int treeId;
    if ((parent != null)) {
      if (parent.getColumn() != 0) {
        return 0;
      }
      Git.Object o = this.getObject(parent);
      if (o.type == Git.ObjectType.Tree) {
        treeId = this.getTreeId(parent.getInternalId(), parent.getRow());
      } else {
        return 0;
      }
    } else {
      treeId = 0;
    }
    return this.treeData_.get(treeId).getRowCount();
  }

  public Object getData(final WModelIndex index, ItemDataRole role) {
    if (!(index != null)) {
      return null;
    }
    Git.Object object = this.getObject(index);
    switch (index.getColumn()) {
      case 0:
        if (role.equals(ItemDataRole.Display)) {
          if (object.type == Git.ObjectType.Tree) {
            return object.name + '/';
          } else {
            return object.name;
          }
        } else {
          if (role.equals(ItemDataRole.Decoration)) {
            if (object.type == Git.ObjectType.Blob) {
              return "icons/git-blob.png";
            } else {
              if (object.type == Git.ObjectType.Tree) {
                return "icons/git-tree.png";
              }
            }
          } else {
            if (role.equals(ContentsRole)) {
              if (object.type == Git.ObjectType.Blob) {
                return this.git_.catFile(object.id);
              }
            }
          }
        }
        break;
      case 1:
        if (role.equals(ItemDataRole.Display)) {
          if (object.type == Git.ObjectType.Tree) {
            return "Folder";
          } else {
            String suffix = getSuffix(object.name);
            if (suffix.equals("C") || suffix.equals("cpp")) {
              return "C++ Source";
            } else {
              if (suffix.equals("h") || suffix.equals("") && !this.topLevel(index)) {
                return "C++ Header";
              } else {
                if (suffix.equals("css")) {
                  return "CSS Stylesheet";
                } else {
                  if (suffix.equals("js")) {
                    return "JavaScript Source";
                  } else {
                    if (suffix.equals("md")) {
                      return "Markdown";
                    } else {
                      if (suffix.equals("png") || suffix.equals("gif")) {
                        return "Image";
                      } else {
                        if (suffix.equals("txt")) {
                          return "Text";
                        } else {
                          return null;
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
    }
    return null;
  }

  public Object getHeaderData(int section, Orientation orientation, ItemDataRole role) {
    if (orientation == Orientation.Horizontal && role.equals(ItemDataRole.Display)) {
      switch (section) {
        case 0:
          return "File";
        case 1:
          return "Type";
        default:
          return null;
      }
    } else {
      return null;
    }
  }

  private Git git_;

  static class ChildIndex {
    private static Logger logger = LoggerFactory.getLogger(ChildIndex.class);

    public int parentId;
    public int index;

    public ChildIndex(int aParent, int anIndex) {
      this.parentId = aParent;
      this.index = anIndex;
    }

    public boolean equals(Object o) {
      GitModel.ChildIndex other = ((ChildIndex) o);
      return this.parentId == other.parentId && this.index == other.index;
    }

    public int hashCode() {
      int hash = 1;
      hash = hash * 31 + this.parentId;
      hash = hash * 31 + this.index;
      return hash;
    }
  }

  static class Tree {
    private static Logger logger = LoggerFactory.getLogger(Tree.class);

    public Tree(int parentId, int index, final Git.ObjectId object, int rowCount) {
      this.index_ = new GitModel.ChildIndex(parentId, index);
      this.treeObject_ = object;
      this.rowCount_ = rowCount;
    }

    public int getParentId() {
      return this.index_.parentId;
    }

    public int getIndex() {
      return this.index_.index;
    }

    public Git.ObjectId getTreeObject() {
      return this.treeObject_;
    }

    public int getRowCount() {
      return this.rowCount_;
    }

    private GitModel.ChildIndex index_;
    private Git.ObjectId treeObject_;
    private int rowCount_;
  }

  private List<GitModel.Tree> treeData_;
  private Map<GitModel.ChildIndex, Integer> childPointer_;

  private int getTreeId(int parentId, int childIndex) {
    GitModel.ChildIndex index = new GitModel.ChildIndex(parentId, childIndex);
    Integer i = this.childPointer_.get(index);
    if (i == null) {
      final GitModel.Tree parentItem = this.treeData_.get(parentId);
      Git.Object o = this.git_.treeGetObject(parentItem.getTreeObject(), childIndex);
      this.treeData_.add(new GitModel.Tree(parentId, childIndex, o.id, this.git_.treeSize(o.id)));
      int result = this.treeData_.size() - 1;
      this.childPointer_.put(index, result);
      return result;
    } else {
      return i;
    }
  }

  private Git.Object getObject(final WModelIndex index) {
    int parentId = index.getInternalId();
    final GitModel.Tree parentItem = this.treeData_.get(parentId);
    return this.git_.treeGetObject(parentItem.getTreeObject(), index.getRow());
  }

  private static String getSuffix(final String fileName) {
    int dot = fileName.lastIndexOf('.');
    if (dot == -1) {
      return "";
    } else {
      return fileName.substring(dot + 1);
    }
  }

  private boolean topLevel(final WModelIndex index) {
    return !(this.getParent(index) != null);
  }
}
