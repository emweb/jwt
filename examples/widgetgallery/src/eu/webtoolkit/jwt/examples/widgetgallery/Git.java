package eu.webtoolkit.jwt.examples.widgetgallery;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.DepthWalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;

public class Git {
	public class ObjectId {
		private org.eclipse.jgit.lib.ObjectId id;

		ObjectId(org.eclipse.jgit.lib.ObjectId id) {
			this.id = id;
		}
	}

	public enum ObjectType {
		Tree, Blob
	}

	public class Object {
		public ObjectId id;
		public ObjectType type;
		public String name;
	}

	private Repository repository;

	public void setRepositoryPath(String repository) {
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try {
			this.repository = builder.setGitDir(new File(repository)).build();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public ObjectId getCommitTree(String revName) {
		try {
			return new ObjectId(repository.resolve(revName));
		} catch (RevisionSyntaxException e) {
			throw new RuntimeException(e);
		} catch (AmbiguousObjectException e) {
			throw new RuntimeException(e);
		} catch (IncorrectObjectTypeException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public int treeSize(ObjectId treeId) {
		try {
			RevWalk walk = new RevWalk(repository, 0);
			RevTree tree = walk.parseTree(treeId.id);
			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(tree);
			treeWalk.setRecursive(false);

			int count = 0;
			while (treeWalk.next())
				++count;
			
			return count;
		} catch (MissingObjectException e) {
			throw new RuntimeException(e);
		} catch (IncorrectObjectTypeException e) {
			throw new RuntimeException(e);
		} catch (CorruptObjectException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Object catFile(ObjectId id) {
		/* Not yet implemented ! */
		return null;
	}

	public Object treeGetObject(ObjectId treeObject, int childIndex) {
		try {
			RevWalk walk = new RevWalk(repository, 0);
			RevTree tree = walk.parseTree(treeObject.id);
			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(tree);
			treeWalk.setRecursive(false);

			for (int i = 0; i <= childIndex; ++i)
				if (!treeWalk.next())
					throw new RuntimeException("No object " + childIndex + " in tree " + treeObject.id.toString());

			Object object = new Object();
			object.id = new ObjectId(treeWalk.getObjectId(0));
			object.type = treeWalk.getFileMode(0) == FileMode.TREE ? ObjectType.Tree : ObjectType.Blob;
			object.name = treeWalk.getNameString();
			return object;
		} catch (MissingObjectException e) {
			throw new RuntimeException(e);
		} catch (IncorrectObjectTypeException e) {
			throw new RuntimeException(e);
		} catch (CorruptObjectException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		Git git = new Git();
		git.setRepositoryPath("/home/koen/project/wt/git/wt/.git");
		ObjectId treeId = git.getCommitTree("master");
		int size = git.treeSize(treeId);
		for (int i = 0; i < size; ++i) {
			Object o = git.treeGetObject(treeId, i);
			System.err.println(o.name + " " + o.id.id + " " + o.type);
		}
	}
}
